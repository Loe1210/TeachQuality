package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Delete("delete from user where id=#{id}")
    int deleteByPrimaryKey(@Param("id") Integer id);

    @Select("select * from user where id=#{id};")
    User selectByPrimaryKey(@Param("id") Integer id);

    /**
     * 根据用户名查询用户信息
     *
     * @param userName
     * @return
     */
    User selectByUserName(String userName);


    /**
     * 根据用户id修改密码
     *
     * @param newPwd
     * @param userId
     * @return
     */
    @Update("update user set user_pwd=#{newPwd} where id=#{id}")
    int updatePwdByUserId(@Param("newPwd") String newPwd, @Param("id") Integer userId);

    /**
     * 根据账号所属部门/指定专业查询账号信息
     *
     * @param userBelong
     * @return
     */
    @Select("select * from user where user_belong=#{userBelong};")
    List<User> selectByUserBelong(@Param("userBelong") String userBelong);

    @Select("select * from user;")
    List<User> selectAll();

    /**
     * 根据账号类型和所属部门/指定专业查询账号信息
     *
     * @param userType
     * @param userBelong
     * @return
     */
    @Select("select * from user where user_type=#{userType} and user_belong=#{userBelong};")
    List<User> selectByUserTypeAndUserBelong(@Param("userType") String userType, @Param("userBelong") String userBelong);


    @Update("update user set user_name=#{userName},real_name=#{realName}," +
            "user_belong=#{userBelong},email=#{email} where id =#{id}")
    boolean updateUser(User user);

    @Select("select real_name from user where id = #{id}")
    String selectUserNameById(Integer id);
}
