package com.vtmer.microteachingquality.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName major_review_evaluation_record
 */
@TableName(value = "major_review_evaluation_record")
@Data
public class MajorReviewEvaluationRecord implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @TableId(type = IdType.AUTO)
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
    /**
     *
     */
    private String evaluationYear;
    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;
}