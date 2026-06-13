package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Hung
 * @date 2022/8/10 16:11
 */
@Mapper
public interface MajorEvaluationProcessMapper extends BaseMapper<MajorEvaluationProcess> {

    @Select("select * from `major_evaluation_process`")
    List<MajorEvaluationProcess> list();
}
