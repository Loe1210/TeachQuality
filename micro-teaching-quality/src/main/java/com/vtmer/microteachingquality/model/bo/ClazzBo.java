package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Hung
 * @date 2022/4/20 17:02
 */
@ApiModel("创建课程输入类")
@Data
public class ClazzBo {
    @NotNull(message = "学院不能为空")
    @ApiModelProperty("学院")
    private String college;
    @NotNull(message = "专业不能为空")
    @ApiModelProperty("专业")
    private String major;
    @NotNull(message = "年级不能为空")
    @ApiModelProperty("年级")
    private String grade;
    @NotNull(message = "课程类型不能为空")
    @ApiModelProperty("课程类型")
    private String type;
    @NotNull(message = "课程名称不能为空")
    @ApiModelProperty("课程名称")
    private String name;
    @NotNull(message = "课程唯一序列号不能为空")
    @ApiModelProperty("课程唯一序列号")
    private String clazzSerialNumber;
}
