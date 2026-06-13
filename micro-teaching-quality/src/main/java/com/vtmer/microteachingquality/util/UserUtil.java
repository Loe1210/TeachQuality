package com.vtmer.microteachingquality.util;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant;
import com.vtmer.microteachingquality.model.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

/**
 * @author Hung
 * @date 2022/4/20 18:47
 */
@Slf4j
public class UserUtil implements UserTypeConstant {

    public static User getCurrentUser() {
        //获取当前user信息
        return JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
    }

    public static List<String> getUserRole() {
        //根据学号获取用户的角色类型，因现在表结构未更改，先使用user表中的type代替
        return getCurrentUser().getRoles();
    }

    public static List<String> getCurrentUserType() {
        //根据学号获取用户的角色类型，因现在表结构未更改，先使用user表中的type代替
        return getUserRole();
    }

    public static List<String> getCurrentUserUserBelong() {
        //根据学号获取用户的角色类型，因现在表结构未更改，先使用user表中的type代替
        return Collections.singletonList(UserUtil.getCurrentUser().getUserBelong());
    }

    public static Boolean isRoleAvailable(String roleName) {
        return getCurrentUserType().contains(roleName);
    }

    public static Boolean isTestRole() {
        List<String> userRole = getUserRole();
        return userRole.contains(TEST_CLAZZ_EVALUATION_EXPERT) || userRole.contains(TEST_CLAZZ_EVALUATION_PRINCIPAL)
                || userRole.contains(TEST_CLAZZ_EVALUATION_EXPERT_LEADER) || userRole.contains(TEST_MAJOR_EVALUATION_EXPERT)
                || userRole.contains(TEST_MAJOR_EVALUATION_PRINCIPAL) || userRole.contains(TEST_MAJOR_EVALUATION_PRINCIPAL_LEADER)
                || userRole.contains(TEST_MAJOR_ARCHIVE_EXPERT) || userRole.contains(TEST_MAJOR_ARCHIVE_PRINCIPAL) || userRole.contains(TEST_MAJOR_ARCHIVE_EXPERT_LEADER);
    }

    public static Boolean isTestRole(User user) {
        List<String> userRole = user.getRoles();
        return userRole.contains(TEST_CLAZZ_EVALUATION_EXPERT) || userRole.contains(TEST_CLAZZ_EVALUATION_PRINCIPAL)
                || userRole.contains(TEST_CLAZZ_EVALUATION_EXPERT_LEADER) || userRole.contains(TEST_MAJOR_EVALUATION_EXPERT)
                || userRole.contains(TEST_MAJOR_EVALUATION_PRINCIPAL) || userRole.contains(TEST_MAJOR_EVALUATION_PRINCIPAL_LEADER)
                || userRole.contains(TEST_MAJOR_ARCHIVE_EXPERT) || userRole.contains(TEST_MAJOR_ARCHIVE_PRINCIPAL) || userRole.contains(TEST_MAJOR_ARCHIVE_EXPERT_LEADER);
    }
}
