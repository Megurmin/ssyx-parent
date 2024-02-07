package com.hj.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.product.SkuImage;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.product.mapper.SkuImageMapper;
import com.hj.ssyx.product.service.SkuImageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HuangJin
 * @date 1:13 2023/9/20
 */
@Service
public class SkuImageServiceImpl extends ServiceImpl<SkuImageMapper, SkuImage> implements SkuImageService {
    @Override
    public List<SkuImage> getImageListBySkuId(Long id) {
        LambdaQueryWrapper<SkuImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuImage::getSkuId, id);
        List<SkuImage> skuImageList = baseMapper.selectList(wrapper);
        return skuImageList;
    }
}
