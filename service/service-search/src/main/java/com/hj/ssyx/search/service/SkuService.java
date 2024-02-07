package com.hj.ssyx.search.service;

import com.hj.ssyx.model.search.SkuEs;
import com.hj.ssyx.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author HuangJin
 * @date 22:52 2023/9/27
 */
public interface SkuService {
    void upperSku(Long skuId);

    void lowerSku(Long skuId);

    List<SkuEs> findHotSkuList();

    Page<SkuEs> search(Pageable pageable, SkuEsQueryVo skuEsQueryVo);

    // 更新商品热度
    void incrHotScore(Long skuId);
}
