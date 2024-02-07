package com.hj.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.product.Attr;
import com.hj.ssyx.product.mapper.AttrMapper;
import com.hj.ssyx.product.service.AttrService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HuangJin
 * @date 23:54 2023/9/19
 */
@Service
public class AttrServiceImpl extends ServiceImpl<AttrMapper, Attr> implements AttrService {
    @Override
    public List<Attr> getAttrListByGroupId(Long groupId) {
        LambdaQueryWrapper<Attr> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Attr::getAttrGroupId, groupId);
        List<Attr> list = baseMapper.selectList(wrapper);
        return list;
    }
}
