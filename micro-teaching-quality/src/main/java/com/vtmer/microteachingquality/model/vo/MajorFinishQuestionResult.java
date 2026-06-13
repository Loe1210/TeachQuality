package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Colin_Knight
 * @create 2023/5/10 2:34
 */
@Data
@ApiModel("查看专业评审流程结果数据集")
@AllArgsConstructor
public class MajorFinishQuestionResult {

    @ApiModelProperty("问题id")
    private Integer optionId;


    @ApiModelProperty("具体选项")
    private String mark;

}
