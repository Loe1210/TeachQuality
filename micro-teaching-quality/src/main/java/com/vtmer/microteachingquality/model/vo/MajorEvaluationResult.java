package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author Colin_Knight
 * @create 2023/5/10 2:28
 */
@Data
@ApiModel("查看课程评审结果")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MajorEvaluationResult {

    @ApiModelProperty("评审流程id")
    private Long majorEvaluationProcessId;

    @ApiModelProperty("该评审专家的id")
    private Integer userId;

    @ApiModelProperty("问题list")
    private List<MajorFinishQuestionResult> majorEvaluationQuestionList;

    @ApiModelProperty("专家意见")
    private String opinion;

    @ApiModelProperty("专家总评")
    private String remark;

    @ApiModelProperty("指标信息")
    private List<OptionResult> optionResultList;


}
