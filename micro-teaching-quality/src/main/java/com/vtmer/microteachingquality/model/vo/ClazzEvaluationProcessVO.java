package com.vtmer.microteachingquality.model.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@ApiModel
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClazzEvaluationProcessVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("创建此评审流程的用户")
    private String creator;
    @ApiModelProperty("此评审所属课程")
    private String clazzName;
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
}
