package com.vtmer.microteachingquality.mapper;

import com.vtmer.microteachingquality.model.pojo.majorevaluation.LeaderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LeaderInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(LeaderInfo record);

    int insertSelective(LeaderInfo record);

    LeaderInfo selectByPrimaryKey(Integer id);

    @Select("select `member_id` from `leader_info` where `leader_id` = #{id}")
    List<String> selectMembersIdByLeaderId(Integer id);

    List<LeaderInfo> selectByLeaderId(Integer leaderId);

    int updateByPrimaryKeySelective(LeaderInfo record);

    int updateByPrimaryKey(LeaderInfo record);
}