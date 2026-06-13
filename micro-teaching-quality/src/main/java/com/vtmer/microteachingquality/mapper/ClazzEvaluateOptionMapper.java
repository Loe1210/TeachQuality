package com.vtmer.microteachingquality.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzEvaluateOption;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzEvaluateOptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClazzEvaluateOptionMapper extends BaseMapper<ClazzEvaluateOptionRecord> {
    int deleteByPrimaryKey(Integer id);


    int insertSelective(ClazzEvaluateOption record);

    ClazzEvaluateOption selectByPrimaryKey(Integer id);

    List<ClazzEvaluateOption> selectByClazzType(String clazzType);

    int updateByPrimaryKeySelective(ClazzEvaluateOption record);

    int updateByPrimaryKey(ClazzEvaluateOption record);

    @Select("select distinct clazz_evaluation_process_id from clazz_evaluate_option_record where user_id=#{userId};")
    List<ClazzEvaluateOptionRecord> selectDistinctEvaluationId(Integer userId);
}