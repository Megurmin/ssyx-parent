package com.hj.ssyx.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.activity.client.ActivityFeignClient;
import com.hj.ssyx.cart.client.CartFeignClient;
import com.hj.ssyx.client.product.ProductFeignClient;
import com.hj.ssyx.client.user.UserFeignClient;
import com.hj.ssyx.common.auth.AuthContextHolder;
import com.hj.ssyx.common.constant.RedisConst;
import com.hj.ssyx.common.exception.SsyxException;
import com.hj.ssyx.common.result.ResultCodeEnum;
import com.hj.ssyx.common.utils.DateUtil;
import com.hj.ssyx.enums.*;
import com.hj.ssyx.model.activity.ActivityRule;
import com.hj.ssyx.model.activity.CouponInfo;
import com.hj.ssyx.model.order.CartInfo;
import com.hj.ssyx.model.order.OrderInfo;
import com.hj.ssyx.model.order.OrderItem;
import com.hj.ssyx.mq.constant.MQConst;
import com.hj.ssyx.mq.service.RabbitService;
import com.hj.ssyx.order.mapper.OrderInfoMapper;
import com.hj.ssyx.order.mapper.OrderItemMapper;
import com.hj.ssyx.order.service.OrderInfoService;
import com.hj.ssyx.vo.order.CartInfoVo;
import com.hj.ssyx.vo.order.OrderConfirmVo;
import com.hj.ssyx.vo.order.OrderSubmitVo;
import com.hj.ssyx.vo.product.SkuStockLockVo;
import com.hj.ssyx.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HuangJin
 * @date 1:38 2024/1/17
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    ActivityFeignClient activityFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitService rabbitService;

    @Autowired
    OrderItemMapper orderItemMapper;

    // 确认订单
    @Override
    public OrderConfirmVo confirmOrder() {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        // 获取用户团长信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        // 获取购物车里面选中的商品
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        // 唯一标识订单
        String orderNo = System.currentTimeMillis() + "";
        redisTemplate.opsForValue().set(RedisConst.ORDER_REPEAT+orderNo,orderNo,24, TimeUnit.HOURS);
        // 获取购物车满足条件的活动和优惠卷信息
        OrderConfirmVo orderConfirmVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        // 封装其他值
        orderConfirmVo.setLeaderAddressVo(leaderAddressVo);
        orderConfirmVo.setOrderNo(orderNo);
        return orderConfirmVo;
    }

    // 生成订单
    @Override
    public Long submitOrder(OrderSubmitVo orderSubmitVo) {
        //第一步 给哪个用户生成订单 设置orderParamVo的userId
        Long userId = AuthContextHolder.getUserId();
        orderSubmitVo.setUserId(userId);
        //第二步 订单不能重复提交
        //1 获取传递过来的订单orderNo
        String orderNo = orderSubmitVo.getOrderNo();
        if (StringUtils.isEmpty(orderNo)) {
            throw new SsyxException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        //2 拿着orderNo到redis进行查询
        String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        //3 如果redis有相同的orderNo，表示正常提交订单，把redis的orderNo删除
        Boolean flag = (Boolean) redisTemplate.execute(new DefaultRedisScript(script, Boolean.class), Arrays.asList(RedisConst.ORDER_REPEAT + orderNo), orderNo);
        //4 如果redis没有相同的orderNo，表示重复提交
        if (!flag) {
            throw new SsyxException(ResultCodeEnum.REPEAT_SUBMIT);
        }
        //第三步 验证库存并锁定库存
        //1 远程调用service-cart模块，获取当前用户购物车选中商品
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        //2 处理不同类型的商品
        List<CartInfo> commonSkuList = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getSkuType() == SkuType.COMMON.getCode())
                .collect(Collectors.toList());
        //3 获取购物车里面的普通类型商品list集合
        if (!CollectionUtils.isEmpty(commonSkuList)) {
            List<SkuStockLockVo> commonStockLockVoList = commonSkuList.stream().map(item -> {
                SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
                skuStockLockVo.setSkuId(item.getSkuId());
                skuStockLockVo.setSkuNum(item.getSkuNum());
                return skuStockLockVo;
            }).collect(Collectors.toList());

            //4 远程调用service-product模块实现锁定商品
            // 验证库存并锁定库存
            Boolean isLockSuccess = productFeignClient.checkAndLock(commonStockLockVoList, orderNo);
            if (!isLockSuccess) {
                throw new SsyxException(ResultCodeEnum.ORDER_STOCK_FALL);
            }
        }
        //第四步 下单
        Long orderId = this.saveOrder(orderSubmitVo,cartInfoList);

        // 下单完成，删除购物车记录
        // 发送mq消息
        rabbitService.sendMessage(MQConst.EXCHANGE_ORDER_DIRECT,
                MQConst.ROUTING_DELETE_CART,orderSubmitVo.getUserId());
        // 第五步 返回订单id
        return orderId;
    }

    @Transactional(rollbackFor = {Exception.class})
    public Long saveOrder(OrderSubmitVo orderSubmitVo, List<CartInfo> cartInfoList) {
        if (CollectionUtils.isEmpty(cartInfoList)) {
            throw new SsyxException(ResultCodeEnum.DATA_ERROR);
        }
        // 查询用户提货点和团长信息
        Long userId = AuthContextHolder.getUserId();
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        if (leaderAddressVo == null) {
            throw new SsyxException(ResultCodeEnum.DATA_ERROR);
        }
        // 计算金额
        // 营销活动金额
        Map<String, BigDecimal> activitySplitAmount = this.computeActivitySplitAmount(cartInfoList);
        // 优惠卷金额
        Map<String, BigDecimal> couponInfoSplitAmount = this.computeCouponInfoSplitAmount(cartInfoList, orderSubmitVo.getCouponId());

        // 封装订单项数据
        List<OrderItem> orderItemList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(null);
            orderItem.setCategoryId(cartInfo.getCategoryId());
            if(cartInfo.getSkuType() == SkuType.COMMON.getCode()) {
                orderItem.setSkuType(SkuType.COMMON);
            } else {
                orderItem.setSkuType(SkuType.SECKILL);
            }
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setImgUrl(cartInfo.getImgUrl());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItem.setLeaderId(orderSubmitVo.getLeaderId());
            // 营销活动金额
            BigDecimal activityAmount = activitySplitAmount.get("activity:" + orderItem.getSkuId());
            if (activityAmount == null) {
                activityAmount = new BigDecimal(0);
            }
            orderItem.setSplitActivityAmount(activityAmount);
            // 优惠卷金额
            BigDecimal couponAmount = couponInfoSplitAmount.get("coupon" + orderItem.getSkuId());
            if (couponAmount == null) {
                couponAmount = new BigDecimal(0);
            }
            orderItem.setSplitCouponAmount(couponAmount);
            // 总金额
            BigDecimal skuTotalAmount = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum()));
            // 优惠之后金额
            BigDecimal splitTotalAmount = skuTotalAmount.subtract(activityAmount).subtract(couponAmount);
            orderItem.setSplitTotalAmount(splitTotalAmount);
            orderItemList.add(orderItem);
        }

        // 封装订单orderInfo数据
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userId);
        orderInfo.setOrderNo(orderSubmitVo.getOrderNo());
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setLeaderId(orderSubmitVo.getLeaderId());
        orderInfo.setLeaderName(leaderAddressVo.getLeaderName());
        orderInfo.setLeaderPhone(leaderAddressVo.getLeaderPhone());
        orderInfo.setTakeName(leaderAddressVo.getTakeName());
        orderInfo.setReceiverName(orderSubmitVo.getReceiverName());
        orderInfo.setReceiverPhone(orderSubmitVo.getReceiverPhone());
        orderInfo.setReceiverProvince(leaderAddressVo.getProvince());
        orderInfo.setReceiverCity(leaderAddressVo.getCity());
        orderInfo.setReceiverDistrict(leaderAddressVo.getDistrict());
        orderInfo.setReceiverAddress(leaderAddressVo.getDetailAddress());
        orderInfo.setWareId(cartInfoList.get(0).getWareId());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        //计算订单金额
        BigDecimal originalTotalAmount = this.computeTotalAmount(cartInfoList);
        BigDecimal activityAmount = activitySplitAmount.get("activity:total");
        if(null == activityAmount) activityAmount = new BigDecimal(0);
        BigDecimal couponAmount = couponInfoSplitAmount.get("coupon:total");
        if(null == couponAmount) couponAmount = new BigDecimal(0);
        BigDecimal totalAmount = originalTotalAmount.subtract(activityAmount).subtract(couponAmount);
        //计算订单金额
        orderInfo.setOriginalTotalAmount(originalTotalAmount);
        orderInfo.setActivityAmount(activityAmount);
        orderInfo.setCouponAmount(couponAmount);
        orderInfo.setTotalAmount(totalAmount);

        //计算团长佣金
        BigDecimal profitRate = new BigDecimal(0);//orderSetService.getProfitRate();
        BigDecimal commissionAmount = orderInfo.getTotalAmount().multiply(profitRate);
        orderInfo.setCommissionAmount(commissionAmount);

        // 添加数据到订单基本信息表
        baseMapper.insert(orderInfo);

        orderItemList.forEach(orderItem -> {
            orderItem.setOrderId(orderInfo.getId());
            orderItemMapper.insert(orderItem);
        });

        // 如果当前订单使用优惠卷，更新优惠卷状态
        if (orderInfo.getCouponId() != null) {
            activityFeignClient.updateCouponInfoUseStatus(orderInfo.getCouponId(),userId,orderInfo.getId());
        }
        //下单成功，记录用户商品购买个数
        String orderSkuKey = RedisConst.ORDER_SKU_MAP + orderSubmitVo.getUserId();
        BoundHashOperations<String, String, Integer> hashOperations = redisTemplate.boundHashOps(orderSkuKey);
        cartInfoList.forEach(cartInfo -> {
            if(hashOperations.hasKey(cartInfo.getSkuId().toString())) {
                Integer orderSkuNum = hashOperations.get(cartInfo.getSkuId().toString()) + cartInfo.getSkuNum();
                hashOperations.put(cartInfo.getSkuId().toString(), orderSkuNum);
            }
        });
        redisTemplate.expire(orderSkuKey, DateUtil.getCurrentExpireTimes(), TimeUnit.SECONDS);
        // 订单id
        return orderInfo.getId();
    }

    // 订单详情
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        // 根据orderId查询订单基本信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        // 根据orderId查询订单所有订单项列表
        List<OrderItem> orderItemList = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderInfo.getId()));
        // 订单项封装
        orderInfo.setOrderItemList(orderItemList);
        return orderInfo;
    }

    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        OrderInfo orderInfo = baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo));
        return orderInfo;
    }

    // 订单支付成功，更新订单状态，扣减库存
    @Override
    public void orderPay(String orderNo) {
        // 查询订单状态是否已经修改完了支付状态
        OrderInfo orderInfo = this.getOrderInfoByOrderNo(orderNo);
        if (orderInfo == null || orderInfo.getOrderStatus() != OrderStatus.UNPAID) {
            return;
        }
        // 更新状态
        this.updateOrderStatus(orderInfo.getId());

        // 扣减库存
        rabbitService.sendMessage(MQConst.EXCHANGE_ORDER_DIRECT,
                MQConst.ROUTING_MINUS_STOCK,
                orderNo);
    }

    private void updateOrderStatus(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        orderInfo.setOrderStatus(OrderStatus.WAITING_DELEVER);
        orderInfo.setProcessStatus(ProcessStatus.WAITING_DELEVER);
        baseMapper.updateById(orderInfo);
    }

    //计算总金额
    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal(0);
        for (CartInfo cartInfo : cartInfoList) {
            BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            total = total.add(itemTotal);
        }
        return total;
    }

    /**
     * 计算购物项分摊的优惠减少金额
     * 打折：按折扣分担
     * 现金：按比例分摊
     * @param cartInfoParamList
     * @return
     */
    private Map<String, BigDecimal> computeActivitySplitAmount(List<CartInfo> cartInfoParamList) {
        Map<String, BigDecimal> activitySplitAmountMap = new HashMap<>();

        //促销活动相关信息
        List<CartInfoVo> cartInfoVoList = activityFeignClient.findCartActivityList(cartInfoParamList);

        //活动总金额
        BigDecimal activityReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(cartInfoVoList)) {
            for(CartInfoVo cartInfoVo : cartInfoVoList) {
                ActivityRule activityRule = cartInfoVo.getActivityRule();
                List<CartInfo> cartInfoList = cartInfoVo.getCartInfoList();
                if(null != activityRule) {
                    //优惠金额， 按比例分摊
                    BigDecimal reduceAmount = activityRule.getReduceAmount();
                    activityReduceAmount = activityReduceAmount.add(reduceAmount);
                    if(cartInfoList.size() == 1) {
                        activitySplitAmountMap.put("activity:"+cartInfoList.get(0).getSkuId(), reduceAmount);
                    } else {
                        //总金额
                        BigDecimal originalTotalAmount = new BigDecimal(0);
                        for(CartInfo cartInfo : cartInfoList) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                        }
                        //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                        BigDecimal skuPartReduceAmount = new BigDecimal(0);
                        if (activityRule.getActivityType() == ActivityType.FULL_REDUCTION) {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                                    //sku分摊金额
                                    BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        } else {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));

                                    //sku分摊金额
                                    BigDecimal skuDiscountTotalAmount = skuTotalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                                    BigDecimal skuReduceAmount = skuTotalAmount.subtract(skuDiscountTotalAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        }
                    }
                }
            }
        }
        activitySplitAmountMap.put("activity:total", activityReduceAmount);
        return activitySplitAmountMap;
    }

    //优惠卷优惠金额
    private Map<String, BigDecimal> computeCouponInfoSplitAmount(List<CartInfo> cartInfoList, Long couponId) {
        Map<String, BigDecimal> couponInfoSplitAmountMap = new HashMap<>();

        if(null == couponId) return couponInfoSplitAmountMap;
        CouponInfo couponInfo = activityFeignClient.findRangeSkuIdList(cartInfoList, couponId);

        if(null != couponInfo) {
            //sku对应的订单明细
            Map<Long, CartInfo> skuIdToCartInfoMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                skuIdToCartInfoMap.put(cartInfo.getSkuId(), cartInfo);
            }
            //优惠券对应的skuId列表
            List<Long> skuIdList = couponInfo.getSkuIdList();
            if(CollectionUtils.isEmpty(skuIdList)) {
                return couponInfoSplitAmountMap;
            }
            //优惠券优化总金额
            BigDecimal reduceAmount = couponInfo.getAmount();
            if(skuIdList.size() == 1) {
                //sku的优化金额
                couponInfoSplitAmountMap.put("coupon:"+skuIdToCartInfoMap.get(skuIdList.get(0)).getSkuId(), reduceAmount);
            } else {
                //总金额
                BigDecimal originalTotalAmount = new BigDecimal(0);
                for (Long skuId : skuIdList) {
                    CartInfo cartInfo = skuIdToCartInfoMap.get(skuId);
                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                    originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                }
                //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                BigDecimal skuPartReduceAmount = new BigDecimal(0);
                if (couponInfo.getCouponType() == CouponType.CASH || couponInfo.getCouponType() == CouponType.FULL_REDUCTION) {
                    for(int i=0, len=skuIdList.size(); i<len; i++) {
                        CartInfo cartInfo = skuIdToCartInfoMap.get(skuIdList.get(i));
                        if(i < len -1) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            //sku分摊金额
                            BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);

                            skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                        } else {
                            BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);
                        }
                    }
                }
            }
            couponInfoSplitAmountMap.put("coupon:total", couponInfo.getAmount());
        }
        return couponInfoSplitAmountMap;
    }
}
