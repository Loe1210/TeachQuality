package com.vtmer.microteachingquality.model.pojo.majorevaluation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用于查询用户所要进行的评审专业
 *
 * @author Xinjie
 * @date 2023/5/8 16:04
 */
@Data
@Accessors(chain = true)
public class MajorEvaluationUser {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 专业id
     */
    private Integer majorId;
}
