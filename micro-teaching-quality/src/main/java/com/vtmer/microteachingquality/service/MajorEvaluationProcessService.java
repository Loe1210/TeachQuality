package com.vtmer.microteachingquality.service;

import com.vtmer.microteachingquality.model.bo.*;
import com.vtmer.microteachingquality.model.vo.FinishedReviewVO;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationGetFileResult;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationProcessInfo;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationProcessSimpleInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Hung
 * @date 2022/8/10 16:07
 */
public interface MajorEvaluationProcessService {

    /**
     * 创建 评审流程
     *
     * @param majorId 专业Id
     * @return 是否创建成功
     */
    Boolean createEvaluationProcess(Integer majorId);

    String deleteEvaluationProcess(Long majorEvaluationProcessId);

    /**
     * 获取该专业的所有评审流程信息
     *
     * @param majorEvaluationListBO 专业id
     * @return 返回评审流程信息
     */
    List<MajorEvaluationProcessSimpleInfoVo> getEvaluationProcesses(SelectMajorEvaluationListBO majorEvaluationListBO);

    String getMajorTypeByProcessId(Long majorEvaluationProcessId);

    Boolean saveDeanEvaluation(DeanEvaluationBO deanEvaluationBO);

    boolean principalUploadMaterial(MultipartFile file, Long majorEvaluationProcessId);


    List<MajorEvaluationGetFileResult> getEvaluationMaterialInformation(Long majorEvaluationProcessId);

    boolean saveMasterEvaluation(MasterEvaluateBO masterEvaluateBO);

    boolean saveLeaderEvaluation(LeaderEvaluateBO leaderEvaluateBO);

    boolean endEvaluationProcess(EndEvaluationProcessBO endEvaluationProcessBO);

    boolean sendBackEvaluation(Long evaluationProcessId, Integer userId);

    MajorEvaluationProcessInfo getEvaluationProcessInfo(Long majorEvaluationId);

    List<FinishedReviewVO> getAllFinishedReviews(Long evaluationProcessId);

    List<FinishedReviewVO> getLeaderFinishedReviews(Long evaluationProcessId);

    boolean updateMasterEvaluation(MasterEvaluateBO masterEvaluateBO);
}
