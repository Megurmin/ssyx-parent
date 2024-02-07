package com.hj.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.product.SkuImage;
import com.hj.ssyx.model.product.SkuPoster;
import com.hj.ssyx.product.mapper.SkuPosterMapper;
import com.hj.ssyx.product.service.SkuPosterService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HuangJin
 * @date 1:18 2023/9/20
 */
@Service
public class SkuPosterServiceImpl extends ServiceImpl<SkuPosterMapper, SkuPoster> implements SkuPosterService {
    @Override
    public List<SkuPoster> getPosterListBySkuId(Long id) {
        LambdaQueryWrapper<SkuPoster> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuPoster::getSkuId, id);
        List<SkuPoster> skuPosterList = baseMapper.selectList(wrapper);
        return skuPosterList;
    }
}
