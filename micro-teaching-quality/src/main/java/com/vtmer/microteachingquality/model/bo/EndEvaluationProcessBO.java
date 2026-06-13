package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author Hung
 * @date 2022/8/12 18:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("结束流程时传输的数据对象")
public class EndEvaluationProcessBO {

    @NotNull(message = "专业评审流程Id为空")
    private String majorEvaluationProcessId;

    private String remark;


}
