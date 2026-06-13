package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveOpinion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MajorArchiveOpinionMapper extends BaseMapper<MajorArchiveOpinion> {
    int deleteByPrimaryKey(Integer id);


    int insertSelective(MajorArchiveOpinion record);

    MajorArchiveOpinion selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MajorArchiveOpinion record);

    int updateByPrimaryKey(MajorArchiveOpinion record);

    MajorArchiveOpinion selectByBatchNameAndMajorIdAndUserId(String batchName, Integer majorId, Integer userId);
}