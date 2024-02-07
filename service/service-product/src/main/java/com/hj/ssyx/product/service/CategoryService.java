package com.hj.ssyx.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.product.Category;
import com.hj.ssyx.vo.product.CategoryQueryVo;

/**
 * @author HuangJin
 * @date 0:22 2023/9/20
 */
public interface CategoryService extends IService<Category> {
    IPage<Category> selectPageCategory(Page<Category> pageParam, CategoryQueryVo categoryQueryVo);
}
