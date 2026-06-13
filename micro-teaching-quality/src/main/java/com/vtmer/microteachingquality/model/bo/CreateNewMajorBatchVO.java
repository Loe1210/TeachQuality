package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Hung
 * @date 2022/11/9 11:21
 */
@ApiModel("创建专业规定批次 入口类")
@Data
public class CreateNewMajorBatchVO {
    @NotNull(message = "专业Id为空")
    @ApiModelProperty("专业Id")
    Integer majorId;
    @NotBlank(message = "批次名为空")
    @ApiModelProperty("批次名")
    String batchName;
}
