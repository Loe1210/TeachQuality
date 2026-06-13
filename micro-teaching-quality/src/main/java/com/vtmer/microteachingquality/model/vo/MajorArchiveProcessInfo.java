package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Hung
 * @date 2022/9/14 23:08
 */
@Data
public class MajorArchiveProcessInfo {
    @ApiModelProperty("创建此评审流程的用户名")
    private String creatorName;
    @ApiModelProperty("创建专业归档流程状态 0:未开始 1:已结束")
    private Integer createProcessStatus;
    @ApiModelProperty("专业归档负责人提交归档材料状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer principalMaterialStatus;
    @ApiModelProperty("专业归档专家评审状态 0:未开始  1:进行中 2:已提交")
    private Integer expertReviewStatus;
    @ApiModelProperty("项目流程结束状态 0:未开始 1:进行中 2:已结束 ")
    private Integer processEndStatus;

    @ApiModelProperty("文件 添加删除权限 0为不可以 1为可以  （可以用来判断是否为创建批次的用户） ")
    private Integer fileReviseAuthority;
    @ApiModelProperty("专业归档专家 评审权限 0为不可以 1为可以")
    private Integer reviewAuthority;
    @ApiModelProperty("查看权限 0为负责人 可以查看所有专家 的全部评审 1为评审专家 只能查看自己的评审")
    private Integer reviewInformationAuthority;

    @ApiModelProperty("此用户是否已经提交了评审 false为未提交 true为已提交")
    private Boolean isReview;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    public MajorArchiveProcessInfo() {
        this.fileReviseAuthority = 0;
        this.reviewAuthority = 0;
        this.reviewInformationAuthority = 0;
        this.isReview = false;
    }
}
