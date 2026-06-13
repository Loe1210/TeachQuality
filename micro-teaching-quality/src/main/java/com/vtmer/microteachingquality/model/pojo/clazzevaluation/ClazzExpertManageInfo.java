package com.vtmer.microteachingquality.model.pojo.clazzevaluation;

import lombok.Data;

import java.io.Serializable;

/**
 * clazz_expert_manage_info
 *
 * @author
 */
@Data
public class ClazzExpertManageInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    /**
     * 课程评审专家的id
     */
    private Integer userId;
    /**
     * 要管理的课程id
     */
    private Integer clazzId;
    /**
     * 评审状态：0未评审，1已经评审
     */
    private String status;


}