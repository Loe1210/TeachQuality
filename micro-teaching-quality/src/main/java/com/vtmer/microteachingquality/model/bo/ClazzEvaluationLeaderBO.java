package com.vtmer.microteachingquality.model.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Hung
 * @date 2022/4/24 22:18
 */
@Data
public class ClazzEvaluationLeaderBO {
    @NotNull(message = "课程id不能为空")
    Integer clazzId;
    @NotNull(message = "课程评审流程id不能为空")
    Long clazzEvaluationProcessId;
    @NotBlank(message = "课程评审意见不能为空")
    String evaluationOpinion;
    String remark;
}
