package com.hj.ssyx.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hj.ssyx.acl.service.AdminRoleService;
import com.hj.ssyx.acl.service.AdminService;
import com.hj.ssyx.acl.service.RoleService;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.common.utils.MD5;
import com.hj.ssyx.model.acl.Admin;
import com.hj.ssyx.vo.acl.AdminQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 15:27 2023/6/20
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/admin/acl/user")
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private RoleService roleService;

    //为用户进行角色分配
    @ApiOperation("为用户进行角色分配")
    @PostMapping("doAssign")
    public Result doAssign(@RequestParam Long adminId,
                           @RequestParam Long[] roleId){
        roleService.saveAdminRole(adminId,roleId);
        return Result.ok(null);
    }

    //获取所有角色和根据用户id查询用户分配角色列表
    @ApiOperation("获取用户角色")
    @GetMapping("toAssign/{adminId}")
    public Result toAssgin(@PathVariable Long adminId){
        Map<String, Object> map = roleService.getRoleByAdminId(adminId);
        return Result.ok(map);
    }
    //1、用户列表
    @ApiOperation("用户列表")
    @GetMapping("{current}/{limit}")
    public Result list(@PathVariable Long current,
                       @PathVariable Long limit,
                       AdminQueryVo adminQueryVo){
        Page<Admin> pageParam = new Page<>(current,limit);
        IPage<Admin> pageModel = adminService.selectPageUser(pageParam, adminQueryVo);
        return Result.ok(pageModel);
    }
    //2、id查询用户
    @ApiOperation("id查询用户")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        Admin admin = adminService.getById(id);
        return Result.ok(admin);
    }
    //3、添加用户
    @ApiOperation("添加用户")
    @PostMapping("save")
    public Result save(@RequestBody Admin admin){
        String passwordMD5 = MD5.encrypt(admin.getPassword());
        admin.setPassword(passwordMD5);
        adminService.save(admin);
        return Result.ok(null);
    }
    //4、修改用户
    @ApiOperation("修改用户")
    @PutMapping("/update")
    public Result update(@RequestBody Admin admin){
        adminService.updateById(admin);
        return Result.ok(null);
    }
    //5、id删除
    @ApiOperation("id删除")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){
        adminService.removeById(id);
        return Result.ok(null);
    }
    //6、批量删除
    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        adminService.removeByIds(idList);
        return Result.ok(null);
    }
}
