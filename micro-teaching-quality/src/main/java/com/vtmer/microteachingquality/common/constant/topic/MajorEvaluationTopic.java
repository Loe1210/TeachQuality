package com.vtmer.microteachingquality.common.constant.topic;

/**
 * @author Hung
 * @date 2022/8/10 17:31
 */
public class MajorEvaluationTopic {
    /**
     * 默认topic
     */
    public static final String MAJOR_EVALUATION = "major_evaluation";

    /**
     * 评审流程创建
     */
    public static final String PROCESS_CREATED = "process_create";

    /**
     * 负责人上传评审材料，此时通知所有评审专家来评审
     */
    public static final String PRINCIPAL_UPLOAD = "material_upload";

    /**
     * 专业负责人上传材料被退回评审
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
