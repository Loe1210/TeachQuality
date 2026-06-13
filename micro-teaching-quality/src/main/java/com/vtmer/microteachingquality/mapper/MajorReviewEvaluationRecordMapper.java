package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.domain.MajorReviewEvaluationRecord;
import org.apache.ibatis.annotations.Delete;

/**
 * @author Cedirc_Adie
 * @description 针对表【major_review_evaluation_record】的数据库操作Mapper
 * @createDate 2023-12-13 20:18:11
 * @Entity com.vtmer.microteachingquality.domain.MajorReviewEvaluationRecord
 */
public interface MajorReviewEvaluationRecordMapper extends BaseMapper<MajorReviewEvaluationRecord> {
    @Delete("delete from `major_review_evaluation_record` where `evaluation_id` = {evaluationId}")
    void deleteByEvaluationId(String evaluationId);
}




