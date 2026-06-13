package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Hung
 * @date 2022/8/26 21:56
 */
@Data
public class ClazzEvaluationProcessInfo {
    @ApiModelProperty("创建此评审流程的用户名")
    private String creatorName;
    @ApiModelProperty("创建课程评审流程状态 0:未开始 1:已结束")
    private Integer createProcessStatus;
    @ApiModelProperty("课程负责人提交材料状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer principalMaterialStatus;
    @ApiModelProperty("评审专家评审状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer expertReviewStatus;
    @ApiModelProperty("评审专家组长评审状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer expertLeaderReviewStatus;
    @ApiModelProperty("评审专家组长小组评审状态 0:未开始  1:进行中 2:已提交")
    private Integer expertGroupReviewStatus;
    @ApiModelProperty(" 0:不需要复评 1:需要复评 2:已复评")
    private Integer assessAgainStatus;
    @ApiModelProperty("项目流程结束状态 0:未开始 1:进行中 2:已结束 ")
    private Integer processEndStatus;

    @ApiModelProperty("文件 添加删除权限 0为不可以 1为可以  （可以用来判断是否为创建评审流程的用户） ")
    private Integer fileReviseAuthority;
    @ApiModelProperty("课程专家  评审权限 0为不可以 1为可以")
    private Integer reviewAuthority;
    @ApiModelProperty("课程专家组长 评审权限(包括自己评审 小组评审 结束流程)  0为不可以 1为可以")
    private Integer reviewLeaderAuthority;
    @ApiModelProperty("查看权限 0为负责人 可以查看所有专家 专家组长 的全部评审 1为评审专家 只能查看自己的评审 2为评审专家组长 可以查看全部专家评审")
    private Integer reviewInformationAuthority;

    @ApiModelProperty("此用户是否已经提交了评审 ")
    private Boolean isReview;

    @ApiModelProperty("此用户如果是专家组长 则是否已经提交了小组评审 ")
    private Boolean isGroupReview;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


    public ClazzEvaluationProcessInfo() {
        this.fileReviseAuthority = 0;
        this.reviewAuthority = 0;
        this.reviewLeaderAuthority = 0;
        this.reviewInformationAuthority = 0;
        this.isGroupReview = false;
        this.isReview = false;
    }
}
