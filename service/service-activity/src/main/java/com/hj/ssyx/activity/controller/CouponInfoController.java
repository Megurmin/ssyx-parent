package com.hj.ssyx.activity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hj.ssyx.activity.service.CouponInfoService;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.activity.CouponInfo;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.vo.activity.CouponRuleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 16:04 2023/10/1
 */
@RestController
@RequestMapping("/admin/activity/couponInfo")
public class CouponInfoController {
    @Autowired
    private CouponInfoService couponInfoService;

    // 1 优惠卷分页查询
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit) {
        IPage<CouponInfo> PageModel = couponInfoService.selectPageCouponInfo(page, limit);
        return Result.ok(PageModel);
    }
    // 2 添加优惠卷
    @PostMapping("save")
    public Result save(@RequestBody CouponInfo couponInfo) {
        couponInfoService.save(couponInfo);
        return Result.ok(null);
    }
    // 3 根据id查询优惠卷
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        CouponInfo couponInfo = couponInfoService.getCouponInfo(id);
        return Result.ok(couponInfo);
    }
    // 4 根据优惠卷id查询规则数据
    @GetMapping("findCouponRuleList/{id}")
    public Result findCouponRuleList(@PathVariable Long id) {
        Map<String,Object> map = couponInfoService.findCouponRuleList(id);
        return Result.ok(map);
    }
    // 5 添加优惠卷规则数据
    @PostMapping("saveCouponRule")
    public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo) {
        couponInfoService.saveCouponRule(couponRuleVo);
        return Result.ok(null);
    }

    // 6 根据关键词查询sku列表
    @GetMapping("findCouponByKeyword/{keyword}")
    public Result findCouponByKeyword(@PathVariable String keyword) {
        List<SkuInfo> skuInfoList = couponInfoService.findCouponByKeyword(keyword);
        return Result.ok(skuInfoList);
    }
}
