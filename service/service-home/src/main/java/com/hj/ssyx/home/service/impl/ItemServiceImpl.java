package com.hj.ssyx.home.service.impl;

import com.hj.ssyx.activity.client.ActivityFeignClient;
import com.hj.ssyx.client.product.ProductFeignClient;
import com.hj.ssyx.client.search.SkuFeignClient;
import com.hj.ssyx.home.service.ItemService;
import com.hj.ssyx.vo.product.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author HuangJin
 * @date 22:34 2023/12/30
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ActivityFeignClient activityFeignClient;
    @Autowired
    private SkuFeignClient skuFeignClient;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Override
    public Map<String, Object> item(Long SkuId, Long userId) {
        HashMap<String, Object> result = new HashMap<>();
        //skuId查询
        CompletableFuture<SkuInfoVo> skuInfoVoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //远程调用获取sku对应数据
            SkuInfoVo skuInfoVo = productFeignClient.getSkuInfoVo(SkuId);
            result.put("skuInfoVo", skuInfoVo);
            return skuInfoVo;
        },threadPoolExecutor);

        //sku对应优惠卷信息
        CompletableFuture<Void> activityCompletableFuture = CompletableFuture.runAsync(() -> {
            //远程调用获取优惠卷
            Map<String, Object> activityMap = activityFeignClient.findActivityAndCoupon(SkuId,userId);
            result.putAll(activityMap);
        },threadPoolExecutor);

        //更新商品热度
        CompletableFuture<Void> hotCompletableFuture = CompletableFuture.runAsync(() -> {
            //远程调用更新热度
            skuFeignClient.incrHotScore(SkuId);
        },threadPoolExecutor);

        //任务组合
        CompletableFuture.allOf(skuInfoVoCompletableFuture,activityCompletableFuture,hotCompletableFuture).join();
        return result;
    }
}
