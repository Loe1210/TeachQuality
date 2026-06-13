package com.vtmer.microteachingquality.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.dto.UserDTO;
import com.vtmer.microteachingquality.model.dto.UserInfoDTO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.vo.UserInfoResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 创建账号
     *
     * @param user
     * @return
     */
    int saveUser(User user);

    /**
     * 修改密码
     *
     * @param loginUser
     * @param newPwd
     * @return
     */
    int updatePwd(User loginUser, String newPwd);

    /**
     * 获取全部账号信息
     *
     * @return
     */
    List<UserInfoResult> listUserInfo(Integer pageNum, Integer pageSize);

    /**
     * 通过用户名获取账号信息
     *
     * @return
     */
    List<UserInfoResult> listUserInfoByRealName(Integer pageNum, Integer pageSize, String realName);

    /**
     * 根据账号所属部门/指定专业获取账号信息
     *
     * @param userBelong
     * @return
     */
    List<UserInfoResult> listUserInfoByUserBelong(String userBelong);

    /**
     * 根据账号类型和所属部门/指定专业获取账号信息
     *
     * @param userType
     * @param userBelong
     * @return
     */
    List<UserInfoResult> listUserInfoByUserTypeAndUserBelong(String userType, String userBelong);

    /**
     * 根据账号id注销(删除)账号
     *
     * @param userId
     * @return
     */
    int cancelAccount(Integer userId);

    /**
     * 根据用户id注销账号
     *
     * @param userId
     * @return
     */
    boolean deleteUserByUserId(Integer userId);

    /**
     * 根据用户id重置账号密码
     *
     * @param userId
     * @return
     */
    int updatePwdByUserId(Integer userId);

    /*    */

    /**
     * 登录界面根据用户信息重置密码
     *//*
    Integer updatePwdByUserNameAndRealNameAndUserBelong(String userName, String realName, String userBelong);*/

    Boolean sendCode(String email);

    @Transactional(rollbackFor = Exception.class)
    int register(UserDTO user);

    Boolean putRole(Integer userId);

    Boolean sendCodeWithForgetting(String email);

    /**
     * 修改个人信息
     *
     * @return
     */
    Boolean updateUserInfo(User user);

    /**
     * 根据姓名查询用户信息
     */
    List<UserInfoDTO> findUserByName(String name);

    /**
     * 根据账号id查询用户信息
     *
     * @param id 账号id
     * @return 用户信息
     * @Author: HJW
     */
    UserInfoResult findUserById(Integer id);

    Boolean createUser(UserDTO userDTO);


    boolean isNeedToRevisePassword();

}
