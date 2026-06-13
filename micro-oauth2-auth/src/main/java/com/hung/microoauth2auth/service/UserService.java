package com.hung.microoauth2auth.service;

import com.hung.microoauth2auth.entity.UserDTO;

import java.util.List;

/**
 * @author Hung
 * @date 2022/4/8 21:50
 */
public interface UserService {
    /**
     * 根据账号查询用户信息
     *
     * @param username
     * @return
     */
    UserDTO getUserByUsername(String username);


    /**
     * 根据用户id查询用户权限
     *
     * @param userId
     * @return
     */
    List<String> findPermissionsByUserId(Integer userId);


    List<String> getUserRole(Integer userId);
}
