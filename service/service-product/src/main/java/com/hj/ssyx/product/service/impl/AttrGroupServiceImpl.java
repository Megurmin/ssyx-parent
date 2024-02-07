package com.hj.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.product.AttrGroup;
import com.hj.ssyx.product.mapper.AttrGroupMapper;
import com.hj.ssyx.product.service.AttrGroupService;
import com.hj.ssyx.vo.product.AttrGroupQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author HuangJin
 * @date 0:20 2023/9/20
 */
@Service
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroup> implements AttrGroupService {
    @Override
    public IPage<AttrGroup> selectPageAttrGroup(Page<AttrGroup> pageParam, AttrGroupQueryVo attrGroupQueryVo) {
        String name = attrGroupQueryVo.getName();
        LambdaQueryWrapper<AttrGroup> wrapper = Wrappers.lambdaQuery();
        if (!StringUtils.isEmpty(name)) {
            wrapper.like(AttrGroup::getName, name);
        }
        Page<AttrGroup> pageModel = baseMapper.selectPage(pageParam, wrapper);
        return pageModel;
    }

    @Override
    public List<AttrGroup> findAllListAttrGroup() {
        LambdaQueryWrapper<AttrGroup> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByDesc(AttrGroup::getId);
        List<AttrGroup> list = baseMapper.selectList(wrapper);
        return list;
    }


}
