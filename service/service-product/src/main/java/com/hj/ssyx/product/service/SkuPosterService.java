package com.hj.ssyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.product.SkuPoster;

import java.util.List;

/**
 * @author HuangJin
 * @date 1:17 2023/9/20
 */
public interface SkuPosterService extends IService<SkuPoster> {
    List<SkuPoster> getPosterListBySkuId(Long id);
}
