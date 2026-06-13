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
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("获取专业归档流程信息")
public class MajorArchiveBatchInfoVO {
    @ApiModelProperty("归档批次id")
    private String batchId;
    @ApiModelProperty("创建此批次的用户")
    private String creatorName;
    @ApiModelProperty("批次名字")
    private String batchName;
    @ApiModelProperty("当前流程状态 四个阶段 流程创建 负责人材料上传 专家评审完成 流程结束")
    private String status;
    @ApiModelProperty("流程创建时间")
    private LocalDateTime createTime;

}
