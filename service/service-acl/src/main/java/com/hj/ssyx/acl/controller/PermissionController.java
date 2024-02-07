package com.hj.ssyx.acl.controller;

import com.hj.ssyx.acl.service.PermissionService;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.acl.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 19:47 2023/6/21
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/admin/acl/permission")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    //查询所有菜单
    @ApiOperation("查询所有菜单")
    @GetMapping()
    public Result list(){
        List<Permission> list = permissionService.queryAllPermission();
        return Result.ok(list);
    }
    //添加菜单
    @ApiOperation("添加菜单")
    @PostMapping("/save")
    public Result save(@RequestBody Permission permission){
        permissionService.save(permission);
        return Result.ok(null);
    }
    //修改菜单
    @ApiOperation("修改菜单")
    @PutMapping("/update")
    public Result update(@RequestBody Permission permission){
        permissionService.updateById(permission);
        return Result.ok(null);
    }
    //递归删除菜单
    @ApiOperation("递归删除菜单")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){
        permissionService.removeChildById(id);
        return Result.ok(null);
    }

    //查询所有权限和根据角色id查询角色分配权限列表
    @ApiOperation("查询角色权限")
    @GetMapping("toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId){
        List<Permission> list = permissionService.getPermissionById(roleId);
        return Result.ok(list);
    }

    //为角色进行权限分配
    @ApiOperation("为角色进行权限分配")
    @PostMapping("doAssign")
    public Result doAssign(@RequestParam Long roleId,
                           @RequestParam Long[] permissionId){
        permissionService.saveRolePermission(roleId,permissionId);
        return Result.ok(null);
    }
}
