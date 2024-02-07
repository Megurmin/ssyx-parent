package com.hj.ssyx.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.activity.mapper.ActivityInfoMapper;
import com.hj.ssyx.activity.mapper.ActivityRuleMapper;
import com.hj.ssyx.activity.mapper.ActivitySkuMapper;
import com.hj.ssyx.activity.service.ActivityInfoService;
import com.hj.ssyx.activity.service.CouponInfoService;
import com.hj.ssyx.client.product.ProductFeignClient;
import com.hj.ssyx.enums.ActivityType;
import com.hj.ssyx.model.activity.ActivityInfo;
import com.hj.ssyx.model.activity.ActivityRule;
import com.hj.ssyx.model.activity.ActivitySku;
import com.hj.ssyx.model.activity.CouponInfo;
import com.hj.ssyx.model.order.CartInfo;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.vo.activity.ActivityRuleVo;
import com.hj.ssyx.vo.order.CartInfoVo;
import com.hj.ssyx.vo.order.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HuangJin
 * @date 16:24 2023/10/1
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {
    @Autowired
    private ActivityRuleMapper activityRuleMapper;

    @Autowired
    private ActivitySkuMapper activitySkuMapper;

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private CouponInfoService couponInfoService;

    @Override
    public IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam) {
        Page<ActivityInfo> activityInfoPage = baseMapper.selectPage(pageParam, null);
        // 分页查询对象里面获取列表数据
        List<ActivityInfo> activityInfoList = activityInfoPage.getRecords();
        activityInfoList.stream().forEach(item -> {
            item.setActivityTypeString(item.getActivityType().getComment());
        });
        return activityInfoPage;
    }

    //1 根据活动id获取活动规则数据
    @Override
    public Map<String, Object> findActivityRuleList(Long activityId) {
        Map<String, Object> result = new HashMap<>();
        // 1 根据活动id查询，查询规则列表 activity_rule表
        LambdaQueryWrapper<ActivityRule> wrapperActivityRule = new LambdaQueryWrapper<>();
        wrapperActivityRule.eq(ActivityRule::getActivityId, activityId);
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(wrapperActivityRule);
        result.put("activityRuleList",activityRuleList);
        // 2 根据活动id查询，查询使用规则的商品列表 activity_sku表
        LambdaQueryWrapper<ActivitySku> wrapperActivitySku = new LambdaQueryWrapper<>();
        wrapperActivitySku.eq(ActivitySku::getActivityId, activityId);
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(wrapperActivitySku);
        // 获取所有skuId
        List<Long> skuIdList = activitySkuList.stream().map(ActivitySku::getSkuId).collect(Collectors.toList());
        // 2.1通过远程调用 service-product模块接口，根据skuId列表得到商品信息
        List<SkuInfo> skuInfoList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(skuIdList)) {
            skuInfoList = productFeignClient.findSkuInfoList(skuIdList);
        }
        result.put("skuInfoList",skuInfoList);
        return result;
    }

    @Override
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        // 第一步 根据活动id删除之前规则数据
        Long activityId = activityRuleVo.getActivityId();
        // ActivityRule数据删除
        activityRuleMapper.delete(new LambdaQueryWrapper<ActivityRule>().eq(ActivityRule::getActivityId,activityId));
        // ActivitySku数据删除
        activitySkuMapper.delete(new LambdaQueryWrapper<ActivitySku>().eq(ActivitySku::getActivityId,activityId));

        // 第二步 添加规则列表数据
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        ActivityInfo activityInfo = baseMapper.selectById(activityId);
        for (ActivityRule activityRule : activityRuleList) {
            activityRule.setActivityId(activityId);
            activityRule.setActivityType(activityInfo.getActivityType());
            activityRuleMapper.insert(activityRule);
        }
        // 第三步 添加规则范围数据
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        for (ActivitySku activitySku : activitySkuList) {
            activitySku.setActivityId(activityId);
            activitySkuMapper.insert(activitySku);
        }
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        // 第一步 根据关键词查询sku匹配内容列表
        // (1) service-product 模块创建接口 根据关键词
        // (2) service-activity 远程调用得到sku内容列表
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        // 判断：如果根据关键词查询不到匹配内容，直接返回空集合
        if (skuInfoList.isEmpty()){
            return skuInfoList;
        }
        // 从skuInfoList集合获取所有skuId
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfo::getId).collect(Collectors.toList());

        // 第二步 判断添加商品之前是否参加过活动，如果之前参加过，活动正在进行中，排除商品
        // (1) 查询两张表判断 activity_info 和 activity_sku，编写SQL语句实现
        List<Long> existSkuIdList = baseMapper.selectSkuIdListExist(skuIdList);
        // (2) 判断逻辑处理 排除已经参加活动的商品
        ArrayList<SkuInfo> findSkuList = new ArrayList<>();
        for (SkuInfo skuInfo : skuInfoList) {
            if (!existSkuIdList.contains(skuInfo.getId())){
                findSkuList.add(skuInfo);
            }
        }
        return findSkuList;
    }

    // 根据skuId列表获取促销信息
    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        Map<Long,List<String>> result = new HashMap<>();
        // skuInfoList遍历，得到每个skuId
        skuIdList.forEach(skuId -> {
            // 根据skuId进行查询，查询sku对应活动里面规则列表
            List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);
            // 数据封装，规则名称
            if (!CollectionUtils.isEmpty(activityRuleList)) {
                List<String> ruleList = new ArrayList<>();
                for (ActivityRule activityRule : activityRuleList) {
                    ruleList.add(getRuleDesc(activityRule));
                }
                result.put(skuId,ruleList);
            }
        });
        return result;
    }

    //根据skuId获取营销数据和优惠卷
    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        //1 根据skuId获取sku营销活动，一个活动有多个规则
        List<ActivityRule> activityRuleList = this.findActivityRuleBySkuId(skuId);
        //2 根据skuId+userId查询优惠卷信息
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfoList(skuId,userId);
        //3 封装到map集合，返回
        Map<String,Object> map = new HashMap<>();
        map.put("couponInfoList",couponInfoList);
        map.put("activityRuleList",activityRuleList);
        return map;
    }

    //获取购物车里面满足条件的优惠卷和活动信息
    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        //1 获取购物车每个购物项参与的活动，根据活动规则分组
        // 一个规则对应多个商品
        List<CartInfoVo> cartInfoVoList = this.findCartActivityList(cartInfoList);
        //2 计算参与活动之后优惠的金额
        BigDecimal activityReduceAmount = cartInfoVoList.stream()
                .filter(cartInfoVo -> cartInfoVo.getActivityRule() != null)
                .map(cartInfoVo -> cartInfoVo.getActivityRule().getReduceAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //3 获取购物车可以使用的优惠卷列表
        List<CouponInfo> couponInfoList = couponInfoService.findCartCouponInfo(cartInfoList,userId);
        //4 计算商品使用优惠卷之后优惠的金额，一次只能使用一张优惠卷
        BigDecimal couponReduceAmount = new BigDecimal(0);
        if (!CollectionUtils.isEmpty(couponInfoList)){
            couponReduceAmount = couponInfoList.stream()
                    .filter(couponInfo -> couponInfo.getIsOptimal() == 1)
                    .map(CouponInfo::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        //5 计算没有参与活动、没有使用优惠卷的原始金额
        BigDecimal originalTotalAmount = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                .map(cartInfo -> cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //6 计算参与活动、使用优惠卷之后的总金额
        BigDecimal totalAmount = originalTotalAmount.subtract(activityReduceAmount).subtract(couponReduceAmount);
        //7 封装需要的数据到OrderConfirmVo，返回
        OrderConfirmVo orderTradeVo = new OrderConfirmVo();
        orderTradeVo.setCarInfoVoList(cartInfoVoList);
        orderTradeVo.setActivityReduceAmount(activityReduceAmount);
        orderTradeVo.setCouponInfoList(couponInfoList);
        orderTradeVo.setCouponReduceAmount(couponReduceAmount);
        orderTradeVo.setOriginalTotalAmount(originalTotalAmount);
        orderTradeVo.setTotalAmount(totalAmount);
        return orderTradeVo;
    }

    // 获取购物车对应规则数据
    @Override
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        // 创建最终返回集合
        List<CartInfoVo> cartInfoVoList = new ArrayList<>();
        // 获取所有skuId
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());
        // 根据所有skuId列表获取参与的活动
        List<ActivitySku> activitySkuList = baseMapper.selectCartActivity(skuIdList);
        // 根据活动进行分组，每个活动里面有哪些skuId信息
        Map<Long, Set<Long>> activityIdToSkuIdListMap = activitySkuList.stream()
                .collect(Collectors.groupingBy(ActivitySku::getActivityId, Collectors.mapping(ActivitySku::getSkuId, Collectors.toSet())));

        // 获取活动里面的规则数据
        // key是活动id，value是活动里面的规则列表数据
        Map<Long,List<ActivityRule>> activityIdToActivityRuleListMap = new HashMap<>();
        // 所有活动id
        Set<Long> activityIdSet = activitySkuList.stream().map(ActivitySku::getActivityId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(activityIdSet)){
            // activity_rule表
            LambdaQueryWrapper<ActivityRule> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ActivityRule::getConditionAmount, ActivityRule::getConditionNum);
            wrapper.in(ActivityRule::getActivityId,activityIdSet);
            List<ActivityRule> activityRuleList = activityRuleMapper.selectList(wrapper);
            // 封装到activityIdToActivityRuleListMap里面
            // 根据活动id进行分组
            activityIdToActivityRuleListMap = activityRuleList.stream().collect(Collectors.groupingBy(ActivityRule::getActivityId));
        }

        // 有活动的购物项skuId
        Set<Long> activitySkuIdSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(activityIdToSkuIdListMap)) {
            // 遍历activityIdToSkuIdListMap集合
            Iterator<Map.Entry<Long, Set<Long>>> iterator = activityIdToSkuIdListMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Set<Long>> entry = iterator.next();
                // 活动id
                Long activityId = entry.getKey();
                // 每个活动对应的skuId列表
                Set<Long> currentActivitySkuIdSet = entry.getValue();
                // 获取当前活动对应的购物项列表
                List<CartInfo> currentActivityCartInfoList = cartInfoList.stream().filter(cartInfo -> currentActivitySkuIdSet.contains(cartInfo.getSkuId())).collect(Collectors.toList());
                // 计算购物项总金额和总数量
                BigDecimal activityTotalAmount = this.computeTotalAmount(currentActivityCartInfoList);
                int activityTotalNum = this.computeCartNum(currentActivityCartInfoList);

                // 计算活动对应规则
                // 根据activityId获取活动对应规则
                List<ActivityRule> currentActivityRuleList = activityIdToActivityRuleListMap.get(activityId);
                ActivityType activityType = currentActivityRuleList.get(0).getActivityType();
                // 判断活动类型：满减和打折
                ActivityRule activityRule = null;
                if (activityType == ActivityType.FULL_REDUCTION) {
                    activityRule = this.computeFullReduction(activityTotalAmount,currentActivityRuleList);
                } else {
                    activityRule = this.computeFullDiscount(activityTotalNum,activityTotalAmount,currentActivityRuleList);
                }

                //CartInfoVo封装
                CartInfoVo cartInfoVo = new CartInfoVo();
                cartInfoVo.setActivityRule(activityRule);
                cartInfoVo.setCartInfoList(currentActivityCartInfoList);
                cartInfoVoList.add(cartInfoVo);

                // 记录哪些购物项参与活动
                activitySkuIdSet.addAll(currentActivitySkuIdSet);
            }
        }
        // 删除参与活动的skuId，获取没有参与活动的skuId
        skuIdList.removeAll(activitySkuIdSet);
        if (!CollectionUtils.isEmpty(skuIdList)) {
            List<CartInfo> noActivityCartInfoList = cartInfoList.stream().filter(cartInfo -> skuIdList.contains(cartInfo.getSkuId())).collect(Collectors.toList());
            CartInfoVo cartInfoVo = new CartInfoVo();
            cartInfoVo.setActivityRule(null);
            cartInfoVo.setCartInfoList(noActivityCartInfoList);
            cartInfoVoList.add(cartInfoVo);
        }
        return cartInfoVoList;
    }

    //根据skuId获取活动规则数据
    @Override
    public List<ActivityRule> findActivityRuleBySkuId(Long skuId) {
        List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);
        for (ActivityRule activityRule:activityRuleList) {
            String ruleDesc = this.getRuleDesc(activityRule);
            activityRule.setRuleDesc(ruleDesc);
        }
        return activityRuleList;
    }

    //构造规则名称的方法
    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuffer ruleDesc = new StringBuffer();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }

    /**
     * 计算满量打折最优规则
     * @param totalNum
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠折扣从大到小排序了
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    /**
     * 计算满减最优规则
     * @param totalAmount
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    private int computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }

}
