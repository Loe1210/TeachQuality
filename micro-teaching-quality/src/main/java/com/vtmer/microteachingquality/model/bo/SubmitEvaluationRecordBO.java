package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Hung
 */
@Data
@ApiModel("课程评审专家上传答题记录")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SubmitEvaluationRecordBO {

    @Valid
    @ApiModelProperty("具体答题记录List")
    private List<InsertEvaluationRecordBO> insertEvaluationRecordBOList;

    @NotNull(message = "课程评审流程id不能为空")
    @ApiModelProperty("评审流程id")
    private Long clazzEvaluationProcessId;

    //@NotNull(message = "专家id不能为空")
    @ApiModelProperty("因为专家组长可以修改专家的评审，所以这里为 原来专家评审的用户id，如果为新增评审，就为专家的id")
    private Integer originReviewExpertId;

    @NotBlank(message = "专家意见不能为空")
    @ApiModelProperty("专家意见")
    private String advantage;

    @NotBlank(message = "专家问题不能为空")
    @ApiModelProperty("专家问题")
    private String problem;

    @NotBlank(message = "专家意见不能为空")
    @ApiModelProperty("专家意见")
    private String advice;

    @NotBlank(message = "总评不能为空")
    @ApiModelProperty("总评价")
    private String remark;

}
