package com.hj.ssyx.acl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.acl.mapper.AdminRoleMapper;
import com.hj.ssyx.acl.service.AdminRoleService;
import com.hj.ssyx.model.acl.AdminRole;
import com.hj.ssyx.model.acl.Role;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HuangJin
 * @date 19:59 2023/6/20
 */
@Service
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {
}
