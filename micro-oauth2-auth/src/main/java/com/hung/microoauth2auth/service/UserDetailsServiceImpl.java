package com.hung.microoauth2auth.service;

import com.alibaba.fastjson.JSON;
import com.hung.microoauth2auth.entity.UserDTO;
import com.hung.microoauth2auth.entity.UserPrincipalDTO;
import com.hung.microoauth2auth.log.annotation.LogRecord;
import com.hung.microoauth2auth.log.context.LogRecordContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理业务类
 *
 * @author Hung
 * @date 2021/11/2 23:07
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    @LogRecord(content = "用户#{#username}登录#{#loginAns}", bizNo = "login")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //将来连接数据库根据账号查询用户信息
        UserDTO userDTO = userService.getUserByUsername(username);
        if (userDTO == null) {
            //如果用户查不到，返回null，由provider来抛出异常
            LogRecordContext.putVariables("loginAns", "失败，用户不存在");
            return null;
        }
        //根据用户的id查询用户的权限
        List<String> permissions = userService.findPermissionsByUserId(userDTO.getId());
        //将permissions转成数组
        String[] permissionArray = new String[permissions.size()];
        permissions.toArray(permissionArray);

        //查询用户的角色
        List<String> userRole = userService.getUserRole(userDTO.getId());

        //将userDto转成json
        UserPrincipalDTO userPrincipalDTO = new UserPrincipalDTO();
        BeanUtils.copyProperties(userDTO, userPrincipalDTO, "userPwd");
        userPrincipalDTO.setRoles(userRole);


        String principal = JSON.toJSONString(userPrincipalDTO);
        LogRecordContext.putVariables("loginAns", "成功");
        return User.withUsername(principal).password(userDTO.getUserPwd()).authorities(permissionArray).build();
    }

}
