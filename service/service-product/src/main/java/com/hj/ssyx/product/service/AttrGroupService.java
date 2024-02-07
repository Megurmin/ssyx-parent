package com.hj.ssyx.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.product.AttrGroup;
import com.hj.ssyx.vo.product.AttrGroupQueryVo;

import java.util.List;

/**
 * @author HuangJin
 * @date 0:19 2023/9/20
 */
public interface AttrGroupService extends IService<AttrGroup> {
    IPage<AttrGroup> selectPageAttrGroup(Page<AttrGroup> pageParam, AttrGroupQueryVo attrGroupQueryVo);

    List<AttrGroup> findAllListAttrGroup();
}
