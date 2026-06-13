package com.vtmer.microteachingquality.model.pojo.clazzevaluation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用于查询用户所要进行的复评课程
 */
@Data
@Accessors(chain = true)
public class ClazzReviewEvaluationUser {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 课程id
     */
    private Integer clazzId;
}
