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
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * clazz_evaluate_option_record
 *
 * @author Hung
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
public class ClazzEvaluateOptionRecord extends Model<ClazzEvaluateOptionRecord> implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty("填写该选项的用户id")
    private Integer userId;
    @ApiModelProperty("被评审流程的id")
    @TableField(jdbcType = JdbcType.BIGINT)
    private Long clazzEvaluationProcessId;
    @ApiModelProperty("课程评价选项的id")
    private Integer evaluationOptionId;
    @ApiModelProperty("具体的选项")
    private String mark;
    @ApiModelProperty("课程Id")
    private Integer clazzId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public ClazzEvaluateOptionRecord(Integer userId, Long clazzEvaluationProcessId, Integer evaluationOptionId, String mark) {
        this.userId = userId;
        this.clazzEvaluationProcessId = clazzEvaluationProcessId;
        this.evaluationOptionId = evaluationOptionId;
        this.mark = mark;
    }
}