package com.vtmer.microteachingquality.service;

import com.vtmer.microteachingquality.model.bo.ReviseUserPermissionBO;

import java.util.List;

/**
 * @author Hung
 */
public interface UserRoleService {

    /**
     * 根据用户id获取角色
     *
     * @param userName
     * @return
     */
    List<String> getUserRole(Integer userId);

    /**
     * 判断用户是否拥有该权限
     */
    boolean isPermissionHave(Integer userId, String permissionId, String ownerId);

    /**
     * 判断用户是否有评审组长/管理员的权限
     */
    boolean isCaptainPermissionHave(Integer userId);

    boolean reviseUserRole(ReviseUserPermissionBO reviseUserPermissionBO);
}
