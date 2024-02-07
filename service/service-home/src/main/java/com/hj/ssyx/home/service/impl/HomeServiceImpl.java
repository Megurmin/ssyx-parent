package com.hj.ssyx.home.service.impl;

import com.hj.ssyx.client.product.ProductFeignClient;
import com.hj.ssyx.client.search.SkuFeignClient;
import com.hj.ssyx.client.user.UserFeignClient;
import com.hj.ssyx.home.service.HomeService;
import com.hj.ssyx.model.product.Category;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.model.search.SkuEs;
import com.hj.ssyx.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 0:22 2023/10/10
 */
@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private SkuFeignClient skuFeignClient;
    @Override
    public Map<String, Object> homeData(Long userId) {
        Map<String,Object> result = new HashMap<>();
        // 1 根据userId获取当前登录语句提货地址信息
        // 远程调用service-user模块接口获取需要的数据
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        result.put("leaderAddressVo", leaderAddressVo);

        // 2 获取所有分类
        // 远程调用service-product模块接口
        List<Category> categoryList = productFeignClient.findAllCategoryList();
        result.put("categoryList", categoryList);

        // 3 获取新人专享商品
        // 远程调用service-product模块接口
        List<SkuInfo> newPersonSkuInfoList = productFeignClient.findNewPersonSkuInfoList();
        result.put("newPersonSkuInfoList",newPersonSkuInfoList);

        // 4 获取爆款商品
        // 远程调用service-search模块接口
        List<SkuEs> hotSkuList = skuFeignClient.findHotSkuList();
        result.put("hotSkuList",hotSkuList);

        // 5 分装获取的数据到map集合，返回
        return result;
    }
}
