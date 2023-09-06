package com.hj.ssyx.acl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.acl.mapper.RolePermissionMapper;
import com.hj.ssyx.acl.service.RolePermissionService;
import com.hj.ssyx.model.acl.RolePermission;
import org.springframework.stereotype.Service;

/**
 * @author HuangJin
 * @date 16:55 2023/6/22
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
