package com.hj.ssyx.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.acl.mapper.RoleMapper;
import com.hj.ssyx.acl.service.AdminRoleService;
import com.hj.ssyx.acl.service.RoleService;
import com.hj.ssyx.model.acl.AdminRole;
import com.hj.ssyx.model.acl.Role;
import com.hj.ssyx.vo.acl.RoleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author HuangJin
 * @date 16:16 2023/6/19
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    @Autowired
    private AdminRoleService adminRoleService;
    @Override
    public IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo) {
        //获取条件值
        String roleName = roleQueryVo.getRoleName();
        //创建mp条件对象
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        //判断条件值是否为空，不为封装查询条件
        if(!StringUtils.isEmpty(roleName)){
            wrapper.like(Role::getRoleName,roleName);
        }
        //调用方法实现条件分页查询
        IPage<Role> rolePage = baseMapper.selectPage(pageParam, wrapper);
        //返回分页对象
        return rolePage;
    }

    @Override
    public Map<String,Object> getRoleByAdminId(Long adminId) {
        //1、查询所有角色
        List<Role> allRolesList = baseMapper.selectList(null);
        //2、根据用户id查询用户分配角色列表
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId,adminId);
        List<AdminRole> adminRoleList = adminRoleService.list(wrapper);
        List<Long> roleIdList = adminRoleList.stream()
                .map(AdminRole::getRoleId)
                .collect(Collectors.toList());
        List<Role> assignRoles = baseMapper.selectBatchIds(roleIdList);

        //封装到map，返回
        HashMap<String,Object> result = new HashMap<>();
        //所有角色
        result.put("allRolesList",allRolesList);
        //用户分配角色列表
        result.put("assignRoles",assignRoles);
        return result;
    }

    @Override
    public void saveAdminRole(Long adminId, Long[] roleIds) {
        //1、删除用户已经分配过的角色数据
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId,adminId);
        adminRoleService.remove(wrapper);
        //2、重新分配
        List<AdminRole> adminRoleList = new ArrayList<>();
        for (Long roleId : roleIds) {
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(adminId);
            adminRole.setRoleId(roleId);
            adminRoleList.add(adminRole);
        }
        adminRoleService.saveBatch(adminRoleList);
    }
}
