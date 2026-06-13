package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Hung
 */
@Data
@ApiModel("课程评审专家上传评审记录")
public class InsertEvaluationRecordBO {

    @ApiModelProperty("评审问题的id")
    private Integer optionId;

    @ApiModelProperty("问题的选项")
    private String mark;

}
