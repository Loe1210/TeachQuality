package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Xinjie
 * @date 2023/5/8 19:45
 */
@Mapper
public interface MajorEvaluationUserMapper extends BaseMapper<MajorEvaluationUser> {
    /**
     * 查询课程id
     *
     * @param userId
     * @return
     */
    List<Integer> selectMajorId(@Param("userId") Integer userId);
}
