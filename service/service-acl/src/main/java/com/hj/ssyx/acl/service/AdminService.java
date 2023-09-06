package com.hj.ssyx.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.acl.Admin;
import com.hj.ssyx.vo.acl.AdminQueryVo;

/**
 * @author HuangJin
 * @date 15:40 2023/6/20
 */
public interface AdminService extends IService<Admin> {
    IPage<Admin> selectPageUser(Page<Admin> pageParam, AdminQueryVo adminQueryVo);
}
