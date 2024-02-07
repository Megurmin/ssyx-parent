package com.hj.ssyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.product.SkuAttrValue;

import java.util.List;

/**
 * @author HuangJin
 * @date 0:40 2023/9/20
 */
public interface SkuAttrValueService extends IService<SkuAttrValue> {
    List<SkuAttrValue> getAttrValueListBySkuId(Long id);
}
