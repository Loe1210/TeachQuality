package com.vtmer.microteachingquality.model.pojo.majorevaluation;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LeaderEvaluation extends Model<LeaderEvaluation> {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Long majorEvaluationProcessId;

    private String result;

    private Integer majorId;

    private String opinion;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public LeaderEvaluation(Integer userId, Long majorEvaluationProcessId, String result, String opinion) {
        this.userId = userId;
        this.majorEvaluationProcessId = majorEvaluationProcessId;
        this.result = result;
        this.opinion = opinion;
    }
}