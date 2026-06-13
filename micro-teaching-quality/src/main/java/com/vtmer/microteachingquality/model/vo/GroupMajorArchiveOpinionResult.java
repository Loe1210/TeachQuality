package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author HJW
 */
@Data
@ApiModel("负责人获取上传的文件信息")
@AllArgsConstructor
@NoArgsConstructor
public class GroupMajorArchiveOpinionResult {

    @ApiModelProperty("所属组别id")
    private Integer groupId;

    @ApiModelProperty("专业名字")
    private String majorName;

    @ApiModelProperty("批次名字")
    private String batchName;

    @ApiModelProperty(value = "专家意见")
    private String opinion;

    @ApiModelProperty(value = "上传该意见的时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "专家id")
    private Integer userId;
}
