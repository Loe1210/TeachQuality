package com.vtmer.microteachingmessage.service;

import com.vtmer.microteachingmessage.pojo.dto.UserMessageDTO;

/**
 * @author Hung
 * @date 2022/8/16 16:35
 */
public interface MajorEvaluationProcessService {

    /**
     * 课程评审流程创建 发送消息到发送人
     *
     * @param userMessageDTO
     */
    <T, F> void evaluationProcessCreate(UserMessageDTO<T, F> userMessageDTO);

    /**
     * 课程评审流程创建 发送消息到发送人
     *
     * @param userMessageDTO
     */
    <T, F> void evaluationProcessPrincipalUpload(UserMessageDTO<T, F> userMessageDTO);

    /**
     * 课程评审流程创建 发送消息到发送人
     *
     * @param userMessageDTO
     */
    <T, F> void evaluationProcessSendBackMaterial(UserMessageDTO<T, F> userMessageDTO);

    /**
     * 课程评审流程创建 发送消息到发送人
     *
     * @param userMessageDTO
     */
    <T, F> void evaluationProcessExpertSubmit(UserMessageDTO<T, F> userMessageDTO);

    /**
     * 课程评审流程创建 发送消息到发送人
     *
     * @param userMessageDTO
     */
    <T, F> void evaluationProcessExpertLeaderSubmit(UserMessageDTO<T, F> userMessageDTO);

}
