package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Hung
 */
@Data
@ApiModel("查看评审结果")
@AllArgsConstructor
@NoArgsConstructor
public class ClazzFinishResult {

    @ApiModelProperty("评审流程id")
    private Long clazzEvaluationProcessId;

    @ApiModelProperty("该评审专家的id")
    private Integer userId;

    @ApiModelProperty("问题list")
    private List<ClazzFinishQuestionResult> clazzFinishQuestionResultList;

    @ApiModelProperty("课程优点")
    private String advantage;

    @ApiModelProperty("课程问题")
    private String problem;

    @ApiModelProperty("课程建议")
    private String advice;

    @ApiModelProperty("课程总评")
    private String remark;

}
