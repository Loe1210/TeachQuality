package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author Hung
 */
@Data
@ApiModel("专家组长提交专业报告评估传输对象")
public class LeaderEvaluateBO {

    @ApiModelProperty(value = "评审选项结果集", notes = "传输格式: '选项id: 达到程度(达到/基本达到/未达到)'", required = true)
    @NotNull(message = "评审选项结果不能为空")
    private Map<Integer, String> optionMap;

    @ApiModelProperty("专业评审流程Id")
    private String majorEvaluationProcessId;

    @ApiModelProperty(value = "评审意见", required = true)
    @NotBlank(message = "评审意见不能为空")
    private String opinion;

    @ApiModelProperty(value = "评审结果", notes = "(合格/不合格)", required = true)
    @NotBlank(message = "评审结果不能为空")
    private String result;

}
