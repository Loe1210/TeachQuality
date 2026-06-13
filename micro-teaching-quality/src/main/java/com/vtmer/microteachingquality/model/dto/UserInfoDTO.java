package com.vtmer.microteachingquality.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author : Gking
 * @date : 2022-07-20 14:38
 **/
@Data
@ApiModel("用于超级管理员或者评审组长查询用户信息时所用的传输对象")
public class UserInfoDTO {
    private String userName;

    private String realName;

    private String userType;

    private String userBelong;

    private String email;
}
