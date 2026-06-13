package com.vtmer.microteachingmessage.constant;

/**
 * 课程评审 Topic 常量类
 *
 * @author Hung
 * @date 2022/5/20 23:44
 */
public class ClazzEvaluationTopic {

    /**
     * 默认topic
     */
    public static final String CLAZZ_EVALUATION = "clazz_evaluation";

    /**
     * 评审流程创建
     */
    public static final String PROCESS_CREATED = "process_create";

    /**
     * 负责人上传评审材料，此时通知所有评审专家来评审
     */
    public static final String PRINCIPAL_UPLOAD = "material_upload";

    /**
     * 课程负责人上传材料被退回评审
     */
    public static final String MATERIAL_BACK = "principal_material_back";

    /**
     * 专家提交自己的评审，通知专家组长评审
     */
    public static final String EXPERT_SUBMIT = "expert_submit";

    /**
     * 专家组长提交自己的评审，通知专家小组发表小组意见
     */
    public static final String EXPERT_LEADER_SUBMIT = "expert_leader_submit";


}


