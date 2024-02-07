package com.hj.ssyx.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hj.ssyx.model.product.SkuInfo;
import org.apache.ibatis.annotations.Param;

/**
 * @author HuangJin
 * @date 0:18 2023/9/20
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {
    // 解锁库存
    void unlockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    // 验证库存
    SkuInfo checkStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    // 锁定库存
    Integer lockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    // 减库存
    void minusStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);
}
