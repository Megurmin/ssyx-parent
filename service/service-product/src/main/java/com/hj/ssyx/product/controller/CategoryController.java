package com.hj.ssyx.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.product.Category;
import com.hj.ssyx.product.service.CategoryService;
import com.hj.ssyx.vo.product.CategoryQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HuangJin
 * @date 23:49 2023/9/19
 */
@Api(value = "Category管理", tags = "商品分类管理")
@RestController
@RequestMapping("/admin/product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    // 商品分类列表
    @ApiOperation("商品分类列表")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       CategoryQueryVo categoryQueryVo) {
        Page<Category> pageParam = new Page<>(page, limit);
        IPage<Category> pageModel = categoryService.selectPageCategory(pageParam, categoryQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取商品分类信息")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.ok(category);
    }

    @ApiOperation(value = "新增商品分类")
    @PostMapping("save")
    public Result save(@RequestBody Category category) {
        categoryService.save(category);
        return Result.ok(null);
    }

    @ApiOperation(value = "修改商品分类")
    @PutMapping("update")
    public Result updateById(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除商品分类")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value = "根据id列表删除商品分类")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        categoryService.removeByIds(idList);
        return Result.ok(null);
    }

    @ApiOperation(value = "获取全部商品分类")
    @GetMapping("findAllList")
    public Result findAllList() {
        return Result.ok(categoryService.list());
    }
}
