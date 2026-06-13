package com.vtmer.microteachingmessage.util;

import com.vtmer.microteachingmessage.pojo.User;

import java.util.Collections;
import java.util.List;

/**
 * @author Hung
 * @date 2022/4/20 18:47
 */
public class UserUtil {

    public static List<String> getUserType(User user) {
        //根据学号获取用户的角色类型，因现在表结构未更改，先使用user表中的type代替
        return Collections.singletonList(user.getUserBelong());
    }

}
