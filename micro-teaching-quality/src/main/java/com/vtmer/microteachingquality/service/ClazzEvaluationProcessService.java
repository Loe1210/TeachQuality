package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.bo.ClazzEvaluationLeaderBO;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.vo.*;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Hung
 * @date 2022/4/20 20:02
 */
public interface ClazzEvaluationProcessService extends IService<ClassEvaluationProcess> {

    /**
     * 创建 评审流程
     *
     * @param clazzId 课程id
     * @return 是否创建成功
     */
    Boolean createEvaluationProcess(Integer clazzId);

    /**
     * 删除 评审流程
     *
     * @param clazzEvaluationProcessId 流程id
     * @return 是否删除成功
     */
    String deleteEvaluationProcess(Long clazzEvaluationProcessId);

    /**
     * 获取该课程的所有评审流程信息
     *
     * @param clazzId 课程id
     * @return 返回评审流程信息
     */
    List<ClazzEvaluationProcessSimpleInfoVO> getEvaluationProcesses(Integer clazzId, Integer pageSize, Integer pageNum, Map<String, String> conditionMap);


    /**
     * 课程负责人上传自评材料
     *
     * @param file                     自评材料
     * @param clazzEvaluationProcessId 课程评审流程id
     * @return 是否上传成功
     */
    Boolean principalUploadMaterial(MultipartFile file, Long clazzEvaluationProcessId) throws MQBrokerException, RemotingException, IOException, InterruptedException, MQClientException;


    /**
     * 课程负责人获取自己上传的文件list
     *
     * @param userId                   用户id
     * @param clazzEvaluationProcessId 课程评审流程id
     * @return 返回文件列表
     */
    List<GetUploadedFilesResult> getUploadedFiles(Integer userId, Long clazzEvaluationProcessId);

    /**
     * 课程评审专家退回评审记录
     *
     * @param clazzEvaluationProcessId 被退回的评审id
     * @return 退回结果
     */
    Boolean sendBackEvaluation(Long clazzEvaluationProcessId);


    /**
     * 获取该评审所有已经评审的专家信息
     *
     * @param evaluationProcessId 评审流程id
     * @return 返回已经评审的专家信息
     */
    List<FinishedReviewVO> getAllFinishedReviews(Long evaluationProcessId);

    /**
     * 保存专家组长的评审内容
     */
    Boolean postClazzEvaluationLeaderReview(ClazzEvaluationLeaderBO evaluationLeaderBO);

    /**
     * 获取此评审流程的专家组长评审列表
     *
     * @param clazzEvaluationProcessId 评审流程id
     * @return 返回专家组长评审列表
     */
    List<ClazzEvaluationLeaderReviewVO> getClazzEvaluationLeaderReviews(Long clazzEvaluationProcessId);

    /**
     * 结束课程评审流程
     *
     * @param clazzEvaluationProcessId 评审流程id
     * @param remark                   评语
     * @return 是否结束成功
     */
    Boolean endClazzEvaluationProcess(Long clazzEvaluationProcessId, String remark);


    /**
     * 退回评审专家的评审信息
     *
     * @param evaluationProcessId 评审流程id
     * @param userId              被退回的评审专家Id
     * @return 是否退回成功
     */
    Boolean sendBackExpertReview(Long evaluationProcessId, Integer userId);

    /**
     * 获取该评审流程的评审信息
     *
     * @param evaluationProcessId 评审流程id
     * @return 返回评审信息
     */
    ClazzEvaluationProcessInfo getEvaluationProcessInfo(Long evaluationProcessId);

    /**
     * 结束专家小组评审
     *
     * @param evaluationProcessId 课程评审流程id
     * @return 是否结束成功
     */
    Boolean endGroupEvaluation(Long evaluationProcessId);

}
