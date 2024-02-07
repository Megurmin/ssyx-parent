package com.hj.ssyx.activity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hj.ssyx.activity.service.ActivityInfoService;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.activity.ActivityInfo;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.vo.activity.ActivityRuleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 16:04 2023/10/1
 */
@RestController
@RequestMapping("/admin/activity/activityInfo")
public class ActivityInfoController {
    @Autowired
    private ActivityInfoService activityInfoService;

    // 列表
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit) {
        Page<ActivityInfo> pageParam = new Page<>(page, limit);
        IPage<ActivityInfo> pageModel = activityInfoService.selectPage(pageParam);
        return Result.ok(pageModel);
    }

    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ActivityInfo activityInfo = activityInfoService.getById(id);
        activityInfo.setActivityTypeString(activityInfo.getActivityType().getComment());
        return Result.ok(activityInfo);
    }

    // 添加活动
    @PostMapping("save")
    public Result save(@RequestBody ActivityInfo activityInfo) {
        activityInfoService.save(activityInfo);
        return Result.ok(null);
    }

    // 更新活动
    @PutMapping("update")
    public Result update(@RequestBody ActivityInfo activityInfo) {
        activityInfoService.updateById(activityInfo);
        return Result.ok(null);
    }

    // 删除活动
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        activityInfoService.removeById(id);
        return Result.ok(null);
    }

    // 营销活动规则相关接口
    // 1 根据活动id获取活动规则数据
    @GetMapping("findActivityRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id) {
        Map<String,Object> activityRuleMap = activityInfoService.findActivityRuleList(id);
        return Result.ok(activityRuleMap);
    }
    // 2 在活动里面添加规则数据
    @PostMapping("saveActivityRule")
    public Result saveActivityRule(@RequestBody ActivityRuleVo activityRuleVo) {
        activityInfoService.saveActivityRule(activityRuleVo);
        return Result.ok(null);
    }
    // 3 根据关键词查询匹配sku信息
    @GetMapping("findSkuInfoByKeyword/{keyword}")
    public Result findSkuInfoByKeyword(@PathVariable String keyword) {
        List<SkuInfo> list = activityInfoService.findSkuInfoByKeyword(keyword);
        return Result.ok(list);
    }
}
