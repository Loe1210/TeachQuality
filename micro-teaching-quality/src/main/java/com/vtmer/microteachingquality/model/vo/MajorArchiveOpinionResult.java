package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * @author HJW
 */
@Data
@ApiModel("获取专业归档表评审内容")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MajorArchiveOpinionResult {


    @ApiModelProperty("该评审专家的Id")
    private String userId;

    @ApiModelProperty("该评审专家的姓名")
    private String userName;

    @ApiModelProperty(value = "专家意见")
    private String opinion;

    @ApiModelProperty(value = "上传该意见的时间")
    private LocalDateTime updateTime;
}
