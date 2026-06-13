package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzEvaluationUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Xinjie
 * @date 2023/5/8 19:47
 */
@Mapper
public interface ClazzEvaluationUserMapper extends BaseMapper<ClazzEvaluationUser> {
    /**
     * 查询课程id
     *
     * @param userId
     * @return
     */
    List<Integer> selectClazzId(@Param("userId") Integer userId);
}
