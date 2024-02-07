package com.hj.ssyx.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.sys.RegionWare;
import com.hj.ssyx.sys.service.RegionWareService;
import com.hj.ssyx.vo.sys.RegionWareQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author HuangJin
 * @date 23:59 2023/9/13
 */
@Api(tags = "开通区域接口")
@RestController
@RequestMapping("admin/sys/regionWare")
public class RegionWareController {
    @Autowired
    RegionWareService regionWareService;
    @ApiOperation("开通区域列表")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       RegionWareQueryVo regionWareQueryVo) {
        Page<RegionWare> pageParam = new Page<>(page, limit);
        IPage<RegionWare> pageModel = regionWareService.selectPageRegionWare(pageParam, regionWareQueryVo);
        return Result.ok(pageModel);
    }

    // 添加开通区域
    @ApiOperation("添加开通区域")
    @PostMapping("save")
    public Result addRegionWare(@RequestBody RegionWare regionWare) {
        regionWareService.saveRegionWare(regionWare);
        return Result.ok(null);
    }

    // 删除开通区域
    @ApiOperation("删除开通区域")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        regionWareService.removeById(id);
        return Result.ok(null);
    }

    // 取消开通区域
    @ApiOperation("取消开通区域")
    @PostMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id,
                               @PathVariable Integer status) {
        regionWareService.updateStatus(id, status);
        return Result.ok(null);
    }
}
