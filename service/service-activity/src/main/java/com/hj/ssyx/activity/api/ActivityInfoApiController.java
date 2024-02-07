package com.hj.ssyx.activity.api;

import com.hj.ssyx.activity.service.ActivityInfoService;
import com.hj.ssyx.activity.service.CouponInfoService;
import com.hj.ssyx.model.activity.CouponInfo;
import com.hj.ssyx.model.order.CartInfo;
import com.hj.ssyx.vo.order.CartInfoVo;
import com.hj.ssyx.vo.order.OrderConfirmVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HuangJin
 * @date 0:25 2023/11/6
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityInfoApiController {
    @Autowired
    private ActivityInfoService activityInfoService;

    @Autowired
    private CouponInfoService couponInfoService;

    @ApiOperation("获取购物车里面满足条件的优惠卷和活动信息")
    @PostMapping("inner/findCartActivityAndCoupon/{userId}")
    public OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList,
                                                    @PathVariable("userId") Long userId) {
        return activityInfoService.findCartActivityAndCoupon(cartInfoList,userId);
    }

    @ApiOperation("根据skuId列表获取促销信息")
    @PostMapping("inner/findActivity")
    public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList){
        return activityInfoService.findActivity(skuIdList);
    }

    @ApiOperation("根据skuId获取营销数据和优惠卷")
    @GetMapping("inner/findActivityAndCoupon/{skuId}/{userId}")
    public Map<String, Object> findActivityAndCoupon(@PathVariable Long skuId,
                                                     @PathVariable Long userId){
        return activityInfoService.findActivityAndCoupon(skuId,userId);
    }

    // 获取购物车对应活动规则数据
    @PostMapping("inner/findCartActivityList")
    List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoList) {
        return activityInfoService.findCartActivityList(cartInfoList);
    }

    // 获取购物车对应优惠卷
    @PostMapping("inner/findRangeSkuIdList/{coupon}")
    public CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo> cartInfoList,
                                         @PathVariable("coupon") Long couponId) {
        return couponInfoService.findRangeSkuIdList(cartInfoList,couponId);
    }

    // 更新优惠卷使用状态
    @GetMapping("inner/updateCouponInfoUseStatus/{couponId}/{userId}/{orderId}")
    public Boolean updateCouponInfoUseStatus(@PathVariable("couponId") Long couponId,
                                             @PathVariable("userId") Long userId,
                                             @PathVariable("orderId") Long orderId) {
        couponInfoService.updateCouponInfoUseStatus(couponId,userId,orderId);
        return true;
    }
}
