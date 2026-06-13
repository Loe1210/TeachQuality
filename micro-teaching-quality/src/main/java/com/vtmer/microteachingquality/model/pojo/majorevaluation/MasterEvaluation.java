package com.vtmer.microteachingquality.model.pojo.majorevaluation;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author Hung
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class MasterEvaluation extends Model<MasterEvaluation> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty("填写此评审流程的用户Id")
    private Integer userId;
    private Long majorEvaluationProcessId;
    private String opinion;
    private String remark;
    private Integer majorId;
    @ApiModelProperty("评审流程状态 0为正常 1为退回")
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    public MasterEvaluation(Integer userId, Long majorEvaluationProcessId, String opinion, String remark) {
        this.userId = userId;
        this.majorEvaluationProcessId = majorEvaluationProcessId;
        this.opinion = opinion;
        this.remark = remark;
        this.status = 0;
    }
}