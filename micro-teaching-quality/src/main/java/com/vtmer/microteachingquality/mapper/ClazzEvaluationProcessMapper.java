package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author Hung
 * @date 2022/4/20 19:41
 */
@Mapper
public interface ClazzEvaluationProcessMapper extends BaseMapper<ClassEvaluationProcess> {
    /**
     * 更新评审流程状态
     */
    @Update("update evaluation_process set current_phases=#{status} where evaluation_id=#{id};")
    Integer updateEvaluationProcess(@Param("id") Long evaluationProcessId, @Param("status") String currentStatus);

    @Select("select creator_id from evaluation_process where evaluation_id=#{evaluationProcessId}")
    Integer selectPrincipalIdByEvaluationId(Long evaluationProcessId);

    @Select("select * from class_evaluation_process where clazz_id=#{clazzId}")
    ClassEvaluationProcess selectEvaluationByClazzId(long clazzId);

    @Update("update clazz_opinion_record set status=1 where clazz_evaluation_process_id =#{evaluationProcessId} and user_id=#{userId}")
    Integer updateClazzOpinionRecordStatus(Long evaluationProcessId, Integer userId);

    @Update("update class_evaluation_process set expert_group_review_status=2 and process_end_status=0 where evaluation_id=#{id};")
    Integer updateEvaluationProcessOnPostLeaderGroupReview(@Param("id") Long evaluationProcessId);

    @Update("update class_evaluation_process set  ")
    Integer updateEvaluationProcessOnReview(@Param("id") Long evaluationProcessId);

    @Update("update class_evaluation_process set process_end_status = 2 and process_result = #{mark} where evaluation_id=#{id};")
    Integer updateEvaluationProcessOnEndProcess(@Param("id") Long evaluationProcessId, String mark);


    @Update("update class_evaluation_process set expert_leader_review_status = 1 and expert_review_status = 2 where evaluation_id=#{id};")
    Integer updateEvaluationProcessOnExpertReview(@Param("id") Long evaluationProcessId);

    @Update("update class_evaluation_process set expert_group_review_status = 1 and expert_leader_review_status =2 where evaluation_id=#{id};")
    Integer updateEvaluationProcessOnExpertLeaderReview(@Param("id") Long evaluationProcessId);
}
