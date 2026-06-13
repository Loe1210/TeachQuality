package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.domain.MajorReviewEvaluationRecord;

/**
 * @author Cedirc_Adie
 * @description 针对表【major_review_evaluation_record】的数据库操作Service
 * @createDate 2023-12-13 20:18:11
 */
public interface MajorReviewEvaluationRecordService extends IService<MajorReviewEvaluationRecord> {

    String insert(long evaluationId, int necessary);

    MajorReviewEvaluationRecord getMajorReviewInfo(long evaluationId);

    Boolean endMajorReviewOpinion(MajorReviewEvaluationRecord majorReviewOpinion);
}
