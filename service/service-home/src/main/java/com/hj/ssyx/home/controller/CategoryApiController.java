package com.hj.ssyx.home.controller;

import com.hj.ssyx.client.product.ProductFeignClient;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.product.Category;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author HuangJin
 * @date 17:43 2023/11/5
 */
@Api(tags = "商品分类")
@RestController
@RequestMapping("api/home")
public class CategoryApiController {
    @Autowired
    private ProductFeignClient productFeignClient;

    // 查询所有分类
    @GetMapping("category")
    public Result categoryList() {
        List<Category> categoryList = productFeignClient.findAllCategoryList();
        return Result.ok(categoryList);
    }
}
