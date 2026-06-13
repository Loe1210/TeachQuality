package com.vtmer.microteachingmessage.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * @author Hung
 * @date 2022/8/17 1:39
 */
public class MajorEvaluationProcess {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 创建此课程用户Id
     */
    private Integer creatorId;
    /**
     * 此评审所属专业Id
     */
    private Integer majorId;
}
