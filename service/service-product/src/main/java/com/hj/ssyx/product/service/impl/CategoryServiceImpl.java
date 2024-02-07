package com.hj.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.product.Category;
import com.hj.ssyx.product.mapper.CategoryMapper;
import com.hj.ssyx.product.service.CategoryService;
import com.hj.ssyx.vo.product.CategoryQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author HuangJin
 * @date 0:23 2023/9/20
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public IPage<Category> selectPageCategory(Page<Category> pageParam, CategoryQueryVo categoryQueryVo) {
        String name = categoryQueryVo.getName();
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            wrapper.like(Category::getName, categoryQueryVo);
        }
        Page<Category> pageModel = baseMapper.selectPage(pageParam, wrapper);
        return pageModel;
    }
}
