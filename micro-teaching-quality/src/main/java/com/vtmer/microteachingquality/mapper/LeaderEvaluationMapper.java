package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.LeaderEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface LeaderEvaluationMapper extends BaseMapper<LeaderEvaluation> {
    int deleteByPrimaryKey(Integer id);

    int insert(LeaderEvaluation record);

    LeaderEvaluation selectByPrimaryKey(Integer id);

    List<LeaderEvaluation> selectAll();

    int updateByPrimaryKey(LeaderEvaluation record);

    /**
     * 根据用户id查询
     *
     * @param userId
     * @return
     */
    List<LeaderEvaluation> selectByUserId(Integer userId);

    /**
     * 根据用户id和专业id查询
     *
     * @param userId
     * @param majorId
     * @return
     */
    List<LeaderEvaluation> selectByUserIdAndMajorId(Integer userId, Integer majorId);

    @Select("SELECT le.`opinion`, le.`result`, le.`create_time`, m.`name`, m.`college`, u.`real_name`\n" +
            "FROM leader_evaluation AS le \n" +
            "JOIN major_evaluation_process AS mep ON mep.`id` = le.`major_evaluation_process_id`\n" +
            "JOIN `user` AS u ON le.`user_id` = u.`id`\n" +
            "JOIN major AS m ON mep.`major_id` = m.`id`\n" +
            "WHERE YEAR(le.`create_time`) = 2024;")
    List<Map<String, Object>> selectAllInYear();
}