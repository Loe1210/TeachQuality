package com.vtmer.microteachingquality.model.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "专业评审流程视图")
public class MajorEvaluationProcessVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "所属专业")
    private String majorName;

    @ApiModelProperty(value = "创建此专业评审流程的用户")
    private String creator;

    @ApiModelProperty("创建专业评审流程状态 0:未开始 1:已结束")
    private Integer createProcessStatus;

    @ApiModelProperty("专业负责人提交材料状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer principalMaterialStatus;

    @ApiModelProperty("专业评审专家评审状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer expertReviewStatus;

    @ApiModelProperty("专业评审专家组长评审状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer expertLeaderReviewStatus;

    @ApiModelProperty(" 0:不需要复评 1:需要复评 2:已复评")
    private Integer assessAgainStatus;

    @ApiModelProperty("项目流程结束状态 0:未开始 1:进行中 2:已结束 ")
    private Integer processEndStatus;

    @ApiModelProperty("专家组长对专业评审流程的评价 分为 合格和不合格 ")
    private String remark;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
