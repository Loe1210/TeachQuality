package com.vtmer.microteachingquality.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@ApiModel("获取专家所有专业归档表导出excel的评审内容")
@AllArgsConstructor
@NoArgsConstructor
public class MajorArchiveExpertRecordDTO {
    @ApiModelProperty("专业归档表id")
    private Integer majorArchiveId;
    @ApiModelProperty("专业名字")
    private String majorName;
    @ApiModelProperty("批次名字")
    private String batchName;
    @ApiModelProperty("评审意见")
    private String opinion;
    @ApiModelProperty("上传该意见的时间")
    private LocalDateTime updateTime;
    @ApiModelProperty("所属小组名字")
    private String groupName;

}
