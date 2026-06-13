package com.hung.microoauth2auth.dao;

import com.hung.microoauth2auth.entity.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Hung
 * @date 2021/11/3 23:06
 */
@Mapper
public interface UserDao {


    /**
     * 根据账号查询用户信息
     *
     * @param username
     * @return
     */
    @Select("select id,user_name,user_pwd,real_name,user_type,user_belong,is_clazz from user where user_name = #{username} limit 1")
    UserDTO getUserByUsername(String username);


    /**
     * 根据用户id查询用户权限
     *
     * @param userId
     * @return
     */
    @Select("SELECT p.authority FROM t_permission p,t_role_permission r, t_user_role u WHERE p.id =r.permission_id AND r.role_id=u.role_id AND u.user_id = #{userId};")
    List<String> findPermissionsByUserId(Integer userId);

    @Select("SELECT r.role_name FROM t_role r, t_user_role u WHERE  r.id=u.role_id AND u.user_id = #{userId}; ")
    List<String> selectRoleById(Integer userId);
}
