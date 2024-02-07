package com.hj.ssyx.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.activity.mapper.CouponInfoMapper;
import com.hj.ssyx.activity.mapper.CouponRangeMapper;
import com.hj.ssyx.activity.mapper.CouponUseMapper;
import com.hj.ssyx.activity.service.CouponInfoService;
import com.hj.ssyx.client.product.ProductFeignClient;
import com.hj.ssyx.enums.CouponRangeType;
import com.hj.ssyx.enums.CouponStatus;
import com.hj.ssyx.model.activity.CouponInfo;
import com.hj.ssyx.model.activity.CouponRange;
import com.hj.ssyx.model.activity.CouponUse;
import com.hj.ssyx.model.order.CartInfo;
import com.hj.ssyx.model.product.Category;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.vo.activity.CouponRuleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.hash.ObjectHashMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HuangJin
 * @date 16:28 2023/10/1
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {
    @Autowired
    private CouponRangeMapper couponRangeMapper;

    @Autowired
    private CouponUseMapper couponUseMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public IPage<CouponInfo> selectPageCouponInfo(Long page, Long limit) {
        Page<CouponInfo> pageParam = new Page<>(page, limit);
        IPage<CouponInfo> couponInfoIPage = baseMapper.selectPage(pageParam,null);
        List<CouponInfo> couponInfoList = couponInfoIPage.getRecords();
        couponInfoList.stream().forEach(item -> {
            item.setCouponTypeString(item.getCouponType().getComment());
            CouponRangeType rangeType = item.getRangeType();
            if (rangeType != null) {
                item.setRangeTypeString(item.getRangeType().getComment());
            }
        });
        return couponInfoIPage;
    }

    @Override
    public CouponInfo getCouponInfo(Long id) {
        CouponInfo couponInfo = baseMapper.selectById(id);
        if (couponInfo.getRangeType() != null) {
            couponInfo.setRangeTypeString(couponInfo.getRangeType().getComment());
        }
        return couponInfo;
    }

    @Override
    public Map<String, Object> findCouponRuleList(Long id) {
        // 第一步 根据优惠卷id查询优惠卷信息
        CouponInfo couponInfo = baseMapper.selectById(id);
        // 第二步 根据优惠卷id查询coupon_range 查询对应range_id
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, couponInfo.getId()));
        // 获取所有range_id
        // 如果规则类型是 SKU      range_id 就是skuId
        // 如果规则类型是 CATEGORY range_id 就是分类Id
        List<Long> rangeIdList = couponRangeList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());

        HashMap<String, Object> result = new HashMap<>();
        // 第三步 分别判断封装不同数据
        if (!CollectionUtils.isEmpty(rangeIdList)) {
            if (couponInfo.getRangeType() == CouponRangeType.SKU){
                // 如果规则类型是 SKU，得到skuId，远程调用根据多个skuId获取对应的sku信息
                List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(rangeIdList);
                result.put("skuInfoList", skuInfoList);
            } else if (couponInfo.getRangeType() == CouponRangeType.CATEGORY){
                // 如果规则类型是 CATEGORY，得到分类Id，远程调用根据多个分类Id值获取对应分类信息
                List<Category> categoryList = productFeignClient.findCategoryList(rangeIdList);
                result.put("categoryList", categoryList);
            }
        }
        return result;
    }

    @Override
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
        // 根据优惠卷id删除规则数据
        couponRangeMapper.delete(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId,couponRuleVo.getCouponId()));
        // 更新优惠卷基本信息
        CouponInfo couponInfo = baseMapper.selectById(couponRuleVo.getCouponId());
        couponInfo.setRangeType(couponRuleVo.getRangeType());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setAmount(couponRuleVo.getAmount());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());
        baseMapper.updateById(couponInfo);

        // 添加优惠卷新规则信息
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            // 设置优惠卷id
            couponRange.setCouponId(couponRuleVo.getCouponId());
            // 添加
            couponRangeMapper.insert(couponRange);
        }
    }

    @Override
    public List<SkuInfo> findCouponByKeyword(String keyword) {
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        return skuInfoList;
    }

    //根据skuId+userId查询优惠卷信息
    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId, Long userId) {
        //远程调用：根据skuId获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        //根据条件查询：skuId + 分类id + userId
        List<CouponInfo> couponInfoList = baseMapper.selectCouponInfoList(skuInfo.getId(),skuInfo.getCategoryId(),userId);
        return couponInfoList;
    }

    //获取购物车可以使用的优惠卷列表
    @Override
    public List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId) {
        //1 根据userId获取用户的全部优惠卷
        //coupon_use  coupon_info
        List<CouponInfo> userAllCouponInfoList = baseMapper.selectCartCouponInfoList(userId);
        if (CollectionUtils.isEmpty(userAllCouponInfoList)) {
            return new ArrayList<>();
        }
        //2 从第一步返回的list集合中，获取所有优惠卷id列表
        List<Long> couponIdList = userAllCouponInfoList.stream().map(CouponInfo::getId).collect(Collectors.toList());
        //3 查询优惠卷对应的范围 coupon_range
        //couponRangeList
        LambdaQueryWrapper<CouponRange> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CouponRange::getCouponId,couponIdList);
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(wrapper);
        //4 获取优惠卷id对应skuId列表
        //  Map<Long,List<Long>>
        Map<Long,List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList,couponRangeList);
        //5 遍历全部优惠卷集合，判断优惠卷类型
        BigDecimal reduceAmount = new BigDecimal(0);
        CouponInfo optimalCouponInfo = null;
        for (CouponInfo couponInfo : userAllCouponInfoList) {
            // 全场通用
            if (CouponRangeType.ALL == couponInfo.getRangeType()) {
                // 全场通用
                // 判断是否满足优惠使用门槛
                // 计算购物车商品的总价
                BigDecimal totalAmount = computeTotalAmount(cartInfoList);
                if (totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0) {
                    couponInfo.setIsSelect(1);
                }
            } else {
                // 优惠卷id获取对应skuId列表
                List<Long> skuIdList = couponIdToSkuIdMap.get(couponInfo.getId());
                // 满足使用范围购物项
                List<CartInfo> currentCartInfoList = cartInfoList.stream()
                        .filter(cartInfo -> skuIdList.contains(cartInfo.getSkuId()))
                        .collect(Collectors.toList());
                BigDecimal totalAmount = computeTotalAmount(currentCartInfoList);
                if (totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0) {
                    couponInfo.setIsSelect(1);
                }
            }
            if (couponInfo.getIsSelect().intValue() == 1 && couponInfo.getAmount().subtract(reduceAmount).doubleValue() > 0) {
                reduceAmount = couponInfo.getAmount();
                optimalCouponInfo = couponInfo;
            }
        }
        //6 返回List<CouponInfo>
        if (null != optimalCouponInfo) {
            optimalCouponInfo.setIsOptimal(1);
        }
        return userAllCouponInfoList;
    }

    @Override
    public CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId) {
        // 根据优惠卷id基本信息查询
        CouponInfo couponInfo = baseMapper.selectById(couponId);
        if (couponInfo == null) {
            return null;
        }
        // 根据couponId查询对应CouponRange数据
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, couponId));
        // 对应sku信息
        Map<Long, List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList, couponRangeList);
        // 遍历map，封装到couponInfo对象
        List<Long> skuIdList = couponIdToSkuIdMap.entrySet().iterator().next().getValue();
        couponInfo.setSkuIdList(skuIdList);
        return couponInfo;
    }

    // 更新优惠卷使用状态
    @Override
    public void updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId) {
        // 根据couponId查询
        CouponUse couponUse = couponUseMapper.selectOne(new LambdaQueryWrapper<CouponUse>()
                .eq(CouponUse::getCouponId, couponId)
                .eq(CouponUse::getUserId, userId));
        // 设置修改值
        couponUse.setCouponStatus(CouponStatus.USED);
        // 调用方法修改
        couponUseMapper.updateById(couponUse);
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

    private Map<Long, List<Long>> findCouponIdToSkuIdMap(List<CartInfo> cartInfoList, List<CouponRange> couponRangeList) {
        Map<Long,List<Long>> couponIdToSkuIdMap = new HashMap<>();

        // couponRangeList数据处理，根据优惠卷id分组
        Map<Long, List<CouponRange>> CouponRangeToRangeListMap = couponRangeList.stream()
                .collect(Collectors.groupingBy(CouponRange::getCouponId, Collectors.toList()));

        // 遍历map集合
        Iterator<Map.Entry<Long, List<CouponRange>>> iterator = CouponRangeToRangeListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, List<CouponRange>> entry = iterator.next();
            Long couponId = entry.getKey();
            List<CouponRange> rangeList = entry.getValue();

            // 创建集合 set
            Set<Long> skuIdSet = new HashSet<>();
            for (CartInfo cartInfo : cartInfoList) {
                for (CouponRange couponRange : rangeList) {
                    // 判断
                    if ((couponRange.getRangeType() == CouponRangeType.SKU && couponRange.getRangeId().longValue() == cartInfo.getSkuId())
                    || (couponRange.getRangeType() == CouponRangeType.CATEGORY && couponRange.getRangeId().longValue() == cartInfo.getCategoryId())
                    || couponRange.getRangeType() == CouponRangeType.ALL) {
                        skuIdSet.add(cartInfo.getSkuId());
                    }
                }
            }
            couponIdToSkuIdMap.put(couponId,new ArrayList<>(skuIdSet));
        }
        return couponIdToSkuIdMap;
    }
}
