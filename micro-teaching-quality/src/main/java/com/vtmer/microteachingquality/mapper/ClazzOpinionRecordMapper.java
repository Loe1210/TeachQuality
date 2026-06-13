package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.dto.ClazzOpinionRecordDTO;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzOpinionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ClazzOpinionRecordMapper extends BaseMapper<ClazzOpinionRecord> {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(ClazzOpinionRecord record);

    ClazzOpinionRecord selectByClazzId(Integer clazzId);

    @Select(" select id, clazz_id, user_id, opinion, create_time, update_time from clazz_opinion_record where user_id = #{userId} and clazz_id = #{clazzId};")
    ClazzOpinionRecord selectOpinionByClazzIdAndUserId(@Param("clazzId") Integer clazzId, @Param("userId") Integer userId);

    ClazzOpinionRecord selectByPrimaryKey(Integer id);

    List<ClazzOpinionRecord> selectAll();

    int updateByPrimaryKeySelective(ClazzOpinionRecord record);

    int updateByPrimaryKey(ClazzOpinionRecord record);

    int deleteByClazzIdAndUserId(Integer clazzId, Integer userId);

    @Select("select user_id,update_time from clazz_opinion_record where clazz_evaluation_process_id = #{evaluationId} ;")
    List<ClazzOpinionRecordDTO> getAllReviewInfo(Long evaluationId);

    @Select("SELECT cor.`clazz_advantage`, cor.`clazz_advice` , cor.`clazz_problem`, cor.`clazz_remark`, c.`college`, c.`name`, u.`real_name`, cor.`create_time`\n" +
            "FROM clazz_opinion_record cor \n" +
            "JOIN class_evaluation_process AS cep ON cep.`evaluation_id` = cor.`clazz_evaluation_process_id`\n" +
            "JOIN `user` AS u ON cor.`user_id` = u.`id`\n" +
            "JOIN clazz AS c ON cep.`clazz_id` = c.`id`\n" +
            "WHERE YEAR(cor.`update_time`) = 2024;")
    List<Map<String, Object>> getAll();
}