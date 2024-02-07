package com.hj.ssyx.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.activity.CouponInfo;
import com.hj.ssyx.model.order.CartInfo;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.vo.activity.CouponRuleVo;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 16:05 2023/10/1
 */
public interface CouponInfoService extends IService<CouponInfo> {
    IPage<CouponInfo> selectPageCouponInfo(Long page, Long limit);

    CouponInfo getCouponInfo(Long id);

    Map<String, Object> findCouponRuleList(Long id);

    void saveCouponRule(CouponRuleVo couponRuleVo);

    List<SkuInfo> findCouponByKeyword(String keyword);

    //根据skuId+userId查询优惠卷信息
    List<CouponInfo> findCouponInfoList(Long skuId, Long userId);

    //获取购物车可以使用的优惠卷列表
    List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId);

    CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId);

    // 更新优惠卷使用状态
    void updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId);
}
