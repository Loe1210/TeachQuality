package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.bo.CreateNewMajorBatchVO;
import com.vtmer.microteachingquality.model.bo.MajorArchiveReviewBO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveBatch;
import com.vtmer.microteachingquality.model.vo.*;

import java.util.List;

/**
 * @author HJW
 */
public interface MajorArchiveProcessService extends IService<MajorArchiveBatch> {


    Boolean createMajorArchiveBatch(CreateNewMajorBatchVO createNewMajorBatchVO);

    Boolean uploadFileRecord(User user, Long majorEvaluationBatchId, String fileName, String path);

    int deleteUploadedFileRecord(User user, String path);

    MajorArchiveGetUploadedFilesInfoResult getUploadedFilesInfo(Long batchId);

    List<MajorArchiveGetTemplateFilesInfoResult> getTemplateFilesInfo();

    int uploadTemplateFileRecord(User user, String fileName, String path);

    int deleteTemplateFileRecord(String fileName, String path);

    List<MajorArchiveGetBatchFilesInfoResult> getBatchFilesInfo(String batchName, Integer majorId);

    /**
     * 获取当前专业+批次的流程信息
     *
     * @param majorId   专业id
     * @param batchName 批次名称
     * @return 流程信息
     */
    List<MajorArchiveBatch> getMajorArchiveProcessList(Integer majorId, String batchName);

    /**
     * 退回评审记录
     *
     * @param majorId   专业id
     * @param batchName 批次名称
     * @return 是否退回成功
     */
    boolean returnEvaluation(Integer majorId, String batchName);

    /**
     * 提交评审记录
     *
     * @return 是否提交成功
     */
    boolean submitEvaluation(MajorArchiveReviewBO majorArchiveReviewBO);


    MajorArchiveProcessInfo getMajorArchiveProcessInfo(Long majorArchiveBatchId);

    /**
     * 获取这个专业的所有批次信息
     *
     * @param majorId 专业id
     * @return 专业归档批次 名字、id、流程状态、创建时间
     */
    List<MajorArchiveBatchInfoVO> getMajorArchiveInfoList(Integer majorId);
}
