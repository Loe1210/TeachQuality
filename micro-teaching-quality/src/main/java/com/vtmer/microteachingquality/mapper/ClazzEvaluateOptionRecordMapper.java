package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzEvaluateOptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClazzEvaluateOptionRecordMapper extends BaseMapper<ClazzEvaluateOptionRecord> {
    int deleteByPrimaryKey(Integer id);

    int insert(ClazzEvaluateOptionRecord record);

    int insertSelective(ClazzEvaluateOptionRecord record);

    int insertList(List<ClazzEvaluateOptionRecord> recordList);

    ClazzEvaluateOptionRecord selectByPrimaryKey(Integer id);

    @Select("select id, user_id, clazz_id, evaluation_option_id, mark, create_time, update_time from clazz_evaluate_option_record where clazz_id= #{clazzId} and user_id= #{userId};")
    List<ClazzEvaluateOptionRecord> selectClazzEvaluateOptionRecordByClazzIdAndUserId(@Param("clazzId") Integer clazzId, @Param("userId") Integer userId);

    int updateByPrimaryKeySelective(ClazzEvaluateOptionRecord record);

    int updateByPrimaryKey(ClazzEvaluateOptionRecord record);

    int deleteByClazzIdAndUserId(Integer clazzId, Integer userId);
}