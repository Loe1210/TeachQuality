package com.vtmer.microteachingmessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingmessage.pojo.ClazzEvaluationProcess;
import com.vtmer.microteachingmessage.pojo.dto.ClazzEvaluationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Hung
 * @date 2022/5/25 19:52
 */
@Mapper
public interface ClazzEvaluationProcessMapper extends BaseMapper<ClazzEvaluationProcess> {

    @Select("select major,grade,name from clazz where id=(select clazz_id from evaluation_procss where evaluation_id=#{id});")
    ClazzEvaluationDTO getClazzEvaluationInfo(@Param("id") Long evaluationProcessId);

}
