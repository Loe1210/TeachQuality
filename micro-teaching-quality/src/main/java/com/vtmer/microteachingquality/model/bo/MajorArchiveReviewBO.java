package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Hung
 * @date 2022/9/7 23:36
 */
@Data
@AllArgsConstructor
public class MajorArchiveReviewBO {
    @NotBlank(message = "未填写评审意见")
    @ApiModelProperty("评审意见")
    String opinion;
    @NotNull(message = "批次Id为空")
    @ApiModelProperty("专业归档批次Id")
    String majorArchiveBatchId;
}
