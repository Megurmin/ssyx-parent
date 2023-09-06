package com.hj.ssyx.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.acl.Role;
import com.hj.ssyx.vo.acl.RoleQueryVo;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 16:03 2023/6/19
 */
public interface RoleService extends IService<Role> {

    IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo);

    Map<String,Object> getRoleByAdminId(Long adminId);

    void saveAdminRole(Long adminId, Long[] roleId);
}
