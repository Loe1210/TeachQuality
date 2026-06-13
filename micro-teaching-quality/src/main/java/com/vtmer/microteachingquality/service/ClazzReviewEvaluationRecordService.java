package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzOpinionRecord;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzReviewEvaluationRecord;
import com.vtmer.microteachingquality.model.vo.ClazzReviewInfo;

import java.util.List;

/**
 * @author Cedirc_Adie
 * @description 针对表【clazz_review_evaluation_record】的数据库操作Service
 * @createDate 2023-12-10 22:36:30
 */
public interface ClazzReviewEvaluationRecordService extends IService<ClazzReviewEvaluationRecord> {

    String insert(long evaluationId, int necessary);

    ClazzReviewInfo getClazzReviewInfo(long evaluationId);

    List<ClazzOpinionRecord> getClazzOpinions(long clazzId);

    Boolean endClazzReviewOpinion(ClazzReviewEvaluationRecord clazzReviewEvaluationRecord);
}
