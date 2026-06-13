package com.vtmer.microteachingquality.model.pojo.majorevaluation;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author HUng
 * @date 2022/8/10 1:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "专业评审流程表")
public class MajorEvaluationProcess extends Model<MajorEvaluationProcess> implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "所属专业id")
    private Integer majorId;

    @ApiModelProperty(value = "创建此专业评审流程的用户Id")
    private Integer creatorId;

    @ApiModelProperty("创建专业评审流程状态 0:未开始 1:已结束")
    private Integer createProcessStatus;

    @ApiModelProperty("专业负责人提交材料状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer principalMaterialStatus;

    @ApiModelProperty("教学院长审核状态 0:未开始  1:进行中 2:已通过")
    private Integer deanReviewStatus;

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

    @ApiModelProperty("评审年份")
    private String evaluationYear;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public MajorEvaluationProcess(Long id, Integer majorId, Integer creatorId, String evaluationYear) {
        this.id = id;
        this.majorId = majorId;
        this.creatorId = creatorId;
        this.createProcessStatus = 1;
        this.principalMaterialStatus = 1;
        this.expertReviewStatus = 0;
        this.expertLeaderReviewStatus = 0;
        this.processEndStatus = 0;
        this.remark = "未评价";
        this.evaluationYear = evaluationYear;
    }
}
