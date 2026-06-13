package com.vtmer.microteachingmessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingmessage.pojo.MajorEvaluationProcess;
import com.vtmer.microteachingmessage.pojo.dto.MajorEvaluationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Hung
 * @date 2022/8/17 1:38
 */
@Mapper
public interface MajorEvaluationProcessMapper extends BaseMapper<MajorEvaluationProcess> {

    @Select("select name,college from major where id=(select major_id from major_evaluation_procss where id=#{id});")
    MajorEvaluationDTO getMajorEvaluationInfo(@Param("id") Long evaluationProcessId);
}
