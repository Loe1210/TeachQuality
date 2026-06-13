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

/**
 * @author Hung
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OptionRecord extends Model<OptionRecord> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer majorId;
    private Integer optionId;
    private Long majorEvaluationProcessId;
    private String mark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    public OptionRecord(Integer userId, Long majorEvaluationProcessId, Integer optionId, String mark) {
        this.userId = userId;
        this.majorEvaluationProcessId = majorEvaluationProcessId;
        this.optionId = optionId;
        this.mark = mark;
    }
}