package com.hj.ssyx.activity.client;

import com.hj.ssyx.model.activity.CouponInfo;
import com.hj.ssyx.model.order.CartInfo;
import com.hj.ssyx.vo.order.CartInfoVo;
import com.hj.ssyx.vo.order.OrderConfirmVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 23:25 2023/11/7
 */
@FeignClient("service-activity")
public interface ActivityFeignClient {

    @ApiOperation("获取购物车里面满足条件的优惠卷和活动信息")
    @PostMapping("/api/activity/inner/findCartActivityAndCoupon/{userId}")
    public OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList,
                                                    @PathVariable("userId") Long userId);
    @PostMapping("/api/activity/inner/findActivity")
    public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList);

    @ApiOperation("根据skuId获取营销数据和优惠卷")
    @GetMapping("/api/activity/inner/findActivityAndCoupon/{skuId}/{userId}")
    public Map<String, Object> findActivityAndCoupon(@PathVariable("skuId") Long skuId,
                                                     @PathVariable("userId") Long userId);

    // 更新优惠卷使用状态
    @GetMapping("/api/activity/inner/updateCouponInfoUseStatus/{couponId}/{userId}/{orderId}")
    public Boolean updateCouponInfoUseStatus(@PathVariable("couponId") Long couponId,
                                             @PathVariable("userId") Long userId,
                                             @PathVariable("orderId") Long orderId);

    // 获取购物车对应规则数据
    @PostMapping("/api/activity/inner/findCartActivityList")
    List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoList);

    // 获取购物车对应优惠卷
    @PostMapping("/api/activity/inner/findRangeSkuIdList/{coupon}")
    public CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo> cartInfoList,
                                         @PathVariable("coupon") Long couponId);
}
