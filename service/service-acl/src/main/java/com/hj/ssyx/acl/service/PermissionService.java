package com.hj.ssyx.acl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.acl.Permission;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 19:45 2023/6/21
 */
public interface PermissionService extends IService<Permission> {
    List<Permission> queryAllPermission();

    void removeChildById(Long id);

    List<Permission> getPermissionById(Long roleId);

    void saveRolePermission(Long roleId, Long[] permissionId);
}
