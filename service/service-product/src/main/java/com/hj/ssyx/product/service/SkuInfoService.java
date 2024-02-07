package com.hj.ssyx.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.vo.product.SkuInfoQueryVo;
import com.hj.ssyx.vo.product.SkuInfoVo;
import com.hj.ssyx.vo.product.SkuStockLockVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HuangJin
 * @date 1:14 2023/9/20
 */
public interface SkuInfoService extends IService<SkuInfo> {
    IPage<SkuInfo> selectPageSkuInfo(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo);

    void saveSkuInfo(SkuInfoVo skuInfoVo);

    SkuInfoVo getSkuInfo(Long id);

    void updateSkuInfo(SkuInfoVo skuInfoVo);

    void check(Long skuId, Integer status);

    void isNewPerson(Long skuId, Integer status);

    void publish(Long skuId, Integer status);

    List<SkuInfo> findSkuInfoList(List<Long> skuIdList);

    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    List<SkuInfo> findNewPersonSkuInfoList();

    SkuInfoVo getSkuInfoVo(Long skuId);

    // 验证和锁定库存
    Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo);

    void minusStock(String orderNo);
}
