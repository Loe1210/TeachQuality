package com.vtmer.microteachingquality.model.pojo.clazzevaluation;

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
 * @author Hung
 * @date 2022/4/19 22:13
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassEvaluationProcess extends Model<ClassEvaluationProcess> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "evaluation_id", type = IdType.ASSIGN_ID)
    private Long id;
    @ApiModelProperty("创建此评审流程的用户Id")
    private Integer creatorId;
    @ApiModelProperty("此评审所属课程Id")
    private Integer clazzId;
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
    @ApiModelProperty("项目流程结束状态 0:未开始 1:进行中 2:已结束 ")
    private Integer processEndStatus;
    @ApiModelProperty(" 0:不需要复评 1:需要复评 2:已复评")
    private Integer assessAgainStatus;
    @ApiModelProperty("评审结果 特优、优秀、良好、尚可、待改进")
    private String processResult;
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @ApiModelProperty("评审年份")
    private String evaluationYear;

    public ClassEvaluationProcess(Long id, Integer creatorId, Integer clazzId, String evaluationYear) {
        this.id = id;
        this.creatorId = creatorId;
        this.clazzId = clazzId;
        this.createProcessStatus = 1;
        this.principalMaterialStatus = 1;
        this.expertReviewStatus = 0;
        this.expertLeaderReviewStatus = 0;
        this.expertGroupReviewStatus = 0;
        this.processEndStatus = 0;
        this.processResult = "";
        this.evaluationYear = evaluationYear;
    }

    public ClassEvaluationProcess(Integer creatorId, Integer clazzId) {
        this.creatorId = creatorId;
        this.clazzId = clazzId;
        this.createProcessStatus = 1;
        this.principalMaterialStatus = 0;
        this.expertReviewStatus = 0;
        this.expertLeaderReviewStatus = 0;
        this.expertGroupReviewStatus = 0;
        this.processEndStatus = 0;
        this.processResult = "";
    }
}
