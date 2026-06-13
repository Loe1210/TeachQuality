package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzReviewEvaluationRecord;
import org.apache.ibatis.annotations.Delete;

/**
 * @author Cedirc_Adie
 * @description 针对表【clazz_review_evaluation_record】的数据库操作Mapper
 * @createDate 2023-12-10 22:36:30
 * @Entity com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzReviewEvaluationRecord
 */
public interface ClazzReviewEvaluationRecordMapper extends BaseMapper<ClazzReviewEvaluationRecord> {

    @Delete("delete from `clazz_review_evaluation_record` where `evaluation_id` = {evaluationId}")
    void deleteByEvaluationId(String evaluationId);
}
