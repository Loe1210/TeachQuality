package com.vtmer.microteachingquality.model.pojo.clazzevaluation;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName clazz_review_evaluation_record
 */
@TableName(value = "clazz_review_evaluation_record")
@Data
public class ClazzReviewEvaluationRecord implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @TableId
    private Integer id;
    /**
     *
     */
    private Long evaluationId;
    /**
     *
     */
    private String reviewEvaluation;
    /**
     *
     */
    private String remark;

    @ApiModelProperty("评审年份")
    private String evaluationYear;
}