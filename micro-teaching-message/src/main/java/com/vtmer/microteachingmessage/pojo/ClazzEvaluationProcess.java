package com.vtmer.microteachingmessage.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Hung
 * @date 2022/4/19 22:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClazzEvaluationProcess extends Model<ClazzEvaluationProcess> {
    @TableId(value = "evaluation_id", type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 创建此课程用户Id
     */
    private Integer creatorId;
    /**
     * 此评审所属课程Id
     */
    private Integer clazzId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public ClazzEvaluationProcess(Long id, Integer creatorId, Integer clazzId, String currentPhases) {
        this.id = id;
        this.creatorId = creatorId;
        this.clazzId = clazzId;

    }
}
