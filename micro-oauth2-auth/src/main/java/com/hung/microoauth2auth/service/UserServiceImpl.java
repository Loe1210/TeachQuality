package com.hung.microoauth2auth.service;

import com.hung.microoauth2auth.dao.UserDao;
import com.hung.microoauth2auth.entity.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author Hung
 * @date 2022/4/8 21:50
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public UserDTO getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }

    @Override
    public List<String> findPermissionsByUserId(Integer userId) {
        return userDao.findPermissionsByUserId(userId);
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public List<String> getUserRole(Integer userId) {
        return userDao.selectRoleById(userId);
    }
}
