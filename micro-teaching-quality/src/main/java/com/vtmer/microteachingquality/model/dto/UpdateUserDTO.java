package com.vtmer.microteachingquality.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author : Gking
 * @date : 2022-07-18 14:23
 **/
@Data
@ApiModel("修改用户信息传输对象")
public class UpdateUserDTO {
    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String userName;

    @ApiModelProperty(value = "真实姓名", required = true)
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @ApiModelProperty(value = "所属部门", required = true)
    @NotBlank(message = "所属部门不能为空")
    private String userBelong;

    @ApiModelProperty(value = "qq邮箱", required = true)
    @NotBlank(message = "qq邮箱不能为空")
    private String email;


}
