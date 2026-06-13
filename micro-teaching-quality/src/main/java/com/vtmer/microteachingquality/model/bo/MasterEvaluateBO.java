package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@ApiModel("评审专家提交专业报告评估传输对象")
public class MasterEvaluateBO {

    @ApiModelProperty(value = "评审选项结果集   , 传输格式: '选项id: 达到程度(达到/基本达到/未达到)'", required = true)
    private Map<Integer, String> optionMap;

    @ApiModelProperty(value = "评审意见", required = true)
    @NotBlank(message = "评审意见为空")
    private String opinion;

    @ApiModelProperty(value = "总评 合格或者不合格")
    @NotBlank(message = "总评为空")
    private String remark;


    @ApiModelProperty("专业评审流程Id")
    @NotNull(message = "专业评审流程Id为空")
    private String majorEvaluationProcessId;

}
