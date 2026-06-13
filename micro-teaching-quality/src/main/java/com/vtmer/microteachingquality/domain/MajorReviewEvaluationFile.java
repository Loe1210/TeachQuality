package com.vtmer.microteachingquality.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName major_review_evaluation_file
 */
@TableName(value = "major_review_evaluation_file")
@Data
public class MajorReviewEvaluationFile implements Serializable {
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
    private long reviewEvaluationId;
    /**
     *
     */
    private String fileName;
    /**
     *
     */
    private String path;
    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;
}