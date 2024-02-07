package com.hj.ssyx.client.search;

import com.hj.ssyx.model.search.SkuEs;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author HuangJin
 * @date 0:33 2023/10/27
 */
@FeignClient("service-search")
public interface SkuFeignClient {
    @GetMapping("/api/search/sku/inner/findHotSkuList")
    public List<SkuEs> findHotSkuList();

    // 更新商品热度
    @GetMapping("/api/search/sku/inner/incrHotScore/{skuId}")
    public Boolean incrHotScore(@PathVariable("skuId") Long skuId);
}
