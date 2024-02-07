package com.hj.ssyx.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.sys.Region;
import com.hj.ssyx.sys.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HuangJin
 * @date 0:28 2023/9/13
 */
@Api(tags = "区域接口")
@RestController
@RequestMapping("admin/sys/region")
public class RegionController {
    @Autowired
    private RegionService regionService;

    @ApiOperation("根据区域关键词查询区域列表信息")
    @GetMapping("findRegionByKeyword/{keyword}")
    public Result findRegionByKeyword(@PathVariable String keyword) {
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Region::getName, keyword);
        List<Region> list = regionService.list(wrapper);
        return Result.ok(list);
    }
}
