package com.hj.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.product.SkuAttrValue;
import com.hj.ssyx.model.product.SkuPoster;
import com.hj.ssyx.product.mapper.SkuAttrValueMapper;
import com.hj.ssyx.product.service.SkuAttrValueService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HuangJin
 * @date 0:41 2023/9/20
 */
@Service
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValue> implements SkuAttrValueService {
    @Override
    public List<SkuAttrValue> getAttrValueListBySkuId(Long id) {
        LambdaQueryWrapper<SkuAttrValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuAttrValue::getSkuId, id);
        List<SkuAttrValue> skuAttrValueList = baseMapper.selectList(wrapper);
        return skuAttrValueList;
    }
}
