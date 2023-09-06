package com.hj.ssyx.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.acl.mapper.PermissionMapper;
import com.hj.ssyx.acl.service.PermissionService;
import com.hj.ssyx.acl.service.RolePermissionService;
import com.hj.ssyx.acl.utils.PermissionHelper;
import com.hj.ssyx.model.acl.AdminRole;
import com.hj.ssyx.model.acl.Permission;
import com.hj.ssyx.model.acl.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HuangJin
 * @date 19:46 2023/6/21
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    @Autowired
    private RolePermissionService rolePermissionService;
    //查询所有菜单
    @Override
    public List<Permission> queryAllPermission() {
        //1、查询所有菜单
        List<Permission> allPermissionList = baseMapper.selectList(null);
        //2、转换要求的数据格式
        List<Permission> result = PermissionHelper.buildPermission(allPermissionList);
        return result;
    }

    //递归删除菜单
    @Override
    public void removeChildById(Long id) {
        //1、传教List集合idList，封装所有要删除菜单id
        ArrayList<Long> idList = new ArrayList<>();
        //根据当前菜单id获取当前菜单下面的所有子菜单
        //如果子菜单下面还有子菜单，都要获取
        //重要：递归找当前菜单的子菜单
        this.getAllPermissionId(id,idList);
        //设置当前菜单id
        idList.add(id);
        //调用方法根据多个菜单id删除
        baseMapper.deleteBatchIds(idList);
    }

    @Override
    public List<Permission> getPermissionById(Long roleId) {
        List<Permission> allPermissionList = baseMapper.selectList(null);

        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId,roleId);
        List<RolePermission> rolePermissionList = rolePermissionService.list(wrapper);
        List<Long> permissionIdList = rolePermissionList.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        List<Permission> tree = PermissionHelper.buildPermission(allPermissionList,permissionIdList);
        return tree;
    }

    @Override
    public void saveRolePermission(Long roleId, Long[] permissionIds) {
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId,roleId);
        rolePermissionService.remove(wrapper);

        List<RolePermission> rolePermissionList = new ArrayList<>();
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionList.add(rolePermission);
        }
        rolePermissionService.saveBatch(rolePermissionList);
    }

    private void getAllPermissionId(Long id, ArrayList<Long> idList) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid,id);
        List<Permission> childList = baseMapper.selectList(wrapper);
        for (Permission permission : childList) {
            idList.add(permission.getId());
            getAllPermissionId(permission.getId(),idList);
        }
    }
}
