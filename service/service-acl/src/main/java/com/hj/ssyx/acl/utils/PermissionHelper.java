package com.hj.ssyx.acl.utils;

import com.hj.ssyx.model.acl.Permission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author HuangJin
 * @date 22:32 2023/6/21
 */
public class PermissionHelper {

    public static List<Permission> buildPermission(List<Permission> allPermissionList) {
        Map<Long, Permission> permissionMap = allPermissionList.stream().collect(Collectors.toMap(Permission::getId, permission -> permission));
        List<Permission> trees = new ArrayList<>();
        for (Permission permission : allPermissionList) {
            Permission permissionParent = permissionMap.get(permission.getPid());
            if(permissionParent==null){
                permission.setLevel(1);
                trees.add(permission);
            }
            else{
                permission.setLevel(permissionParent.getLevel() + 1);
                if(permissionParent.getChildren()==null)
                    permissionParent.setChildren(new ArrayList<>());
                permissionParent.getChildren().add(permission);
            }
        }
        return trees;
    }

    public static List<Permission> buildPermission(List<Permission> allPermissionList,List<Long> selectIdList) {
        Map<Long, Permission> permissionMap = allPermissionList.stream().collect(Collectors.toMap(Permission::getId, permission -> permission));
        HashSet<Long> idSelectSet = new HashSet<>(selectIdList);
        List<Permission> trees = new ArrayList<>();
        for (Permission permission : allPermissionList) {
            Permission permissionParent = permissionMap.get(permission.getPid());
            if(idSelectSet.contains(permission.getId()))
                permission.setSelect(true);
            if(permissionParent==null){
                permission.setLevel(1);
                trees.add(permission);
            }
            else{
                permission.setLevel(permissionParent.getLevel() + 1);
                if(permissionParent.getChildren()==null)
                    permissionParent.setChildren(new ArrayList<>());
                permissionParent.getChildren().add(permission);
            }
        }
        return trees;
    }
}
