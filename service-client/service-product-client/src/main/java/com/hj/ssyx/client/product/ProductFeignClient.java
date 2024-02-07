package com.hj.ssyx.client.product;

import com.hj.ssyx.model.product.Category;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.vo.product.SkuInfoVo;
import com.hj.ssyx.vo.product.SkuStockLockVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author HuangJin
 * @date 0:54 2023/9/28
 */
@FeignClient(value = "service-product")
public interface ProductFeignClient {

    // 获取新人专享商品
    @GetMapping("api/product/inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList();

    // 获取所有分类
    @GetMapping("api/product/inner/findAllCategoryList")
    public List<Category> findAllCategoryList();

    @GetMapping("/api/product/inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable() Long categoryId);

    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId);

    @PostMapping("/api/product/inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIdList);

    @GetMapping("/api/product/inner/findSkuInfoByKeyword/{keyword}")
    public List<SkuInfo> findSkuInfoByKeyword(@PathVariable("keyword") String keyword);

    @PostMapping("/api/product/inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryIdList);

    @GetMapping("/api/product/inner/getSkuInfoVo/{skuId}")
    public SkuInfoVo getSkuInfoVo(@PathVariable Long skuId);

    // 验证和锁定库存
    @PostMapping("/api/product/inner/checkAndLock/{orderNo}")
    public Boolean checkAndLock(@RequestBody List<SkuStockLockVo> skuStockLockVoList,
                                @PathVariable String orderNo);
}
