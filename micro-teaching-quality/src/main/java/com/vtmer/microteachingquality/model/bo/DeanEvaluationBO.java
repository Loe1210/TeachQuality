package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("院长审核时传输的数据对象")
public class DeanEvaluationBO {
    @NotNull(message = "专业评审流程Id为空")
    private String majorEvaluationProcessId;

    private Integer pass;
}
