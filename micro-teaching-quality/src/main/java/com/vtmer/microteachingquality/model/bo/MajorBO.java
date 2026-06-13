package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author Hung
 * @date 2022/8/10 1:17
 */
@ApiModel("新建专业传输对象")
@Data

public class MajorBO {

    /**
     * 专业类型
     */
    @ApiModelProperty(value = "专业类型")
    @NotBlank(message = "专业类型不能为空")
    private String type;

    /**
     * 专业名称
     */
    @ApiModelProperty(value = "专业名称")
    @NotBlank(message = "专业名称不能为空")
    private String name;

    /**
     * 专业所属的学院
     */
    @ApiModelProperty(value = "专业所属的学院")
    @NotBlank(message = "专业所属的学院不能为空")
    private String college;

}
