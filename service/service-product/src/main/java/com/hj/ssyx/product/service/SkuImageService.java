package com.hj.ssyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.product.SkuImage;

import java.util.List;

/**
 * @author HuangJin
 * @date 1:12 2023/9/20
 */
public interface SkuImageService extends IService<SkuImage> {
    List<SkuImage> getImageListBySkuId(Long id);
}
