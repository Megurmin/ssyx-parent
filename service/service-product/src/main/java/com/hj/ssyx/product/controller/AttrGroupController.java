package com.hj.ssyx.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.product.AttrGroup;
import com.hj.ssyx.product.service.AttrGroupService;
import com.hj.ssyx.vo.product.AttrGroupQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HuangJin
 * @date 23:49 2023/9/19
 */
@Api(tags = "")
@RestController
@RequestMapping("/admin/product/attrGroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @ApiOperation("平台属性分组列表")
    @GetMapping("/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       AttrGroupQueryVo attrGroupQueryVo) {
        Page<AttrGroup> pageParam = new Page<>(page, limit);
        IPage<AttrGroup> pageModel = attrGroupService.selectPageAttrGroup(pageParam, attrGroupQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("查询所有平台属性分组列表")
    @GetMapping("/findAllList")
    public Result findAllList(){
        List<AttrGroup> list = attrGroupService.findAllListAttrGroup();
        return Result.ok(list);
    }

    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        AttrGroup attrGroup = attrGroupService.getById(id);
        return Result.ok(attrGroup);
    }

    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody AttrGroup attrGroup) {
        attrGroupService.save(attrGroup);
        return Result.ok(null);
    }

    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody AttrGroup attrGroup) {
        attrGroupService.updateById(attrGroup);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        attrGroupService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        attrGroupService.removeByIds(idList);
        return Result.ok(null);
    }
}
