package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MasterEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MasterEvaluationMapper extends BaseMapper<MasterEvaluation> {
    int deleteByPrimaryKey(Integer id);

    int insert(MasterEvaluation record);

    MasterEvaluation selectByPrimaryKey(Integer id);

    List<MasterEvaluation> selectAll();

    int updateByPrimaryKey(MasterEvaluation record);

    /**
     * 根据用户id查询评审意见
     *
     * @param userId
     * @return
     */
    List<MasterEvaluation> selectByUserId(Integer userId);


    /**
     * 根据用户id和专业id获取评审意见
     *
     * @param userId
     * @param majorId
     * @return
     */
    List<MasterEvaluation> selectByUserIdAndMajorId(Integer userId, Integer majorId);

    /**
     * 根据用户id修改评估状态
     *
     * @param userId
     * @param status
     * @return
     */
    int updateStatusByUserId(Integer userId, String status);

    /**
     * 根据用户id和专业id更新评估状态
     *
     * @param userId
     * @param status
     * @return
     */
    int updateStatusByUserIdAndMajorId(Integer userId, Integer majorId, String status);

    @Select("SELECT me.`opinion`, me.`remark`, me.`create_time`, m.`name`, m.`college`, u.`real_name`\n" +
            "FROM master_evaluation AS me \n" +
            "JOIN major_evaluation_process AS mep ON mep.`id` = me.`major_evaluation_process_id`\n" +
            "JOIN `user` AS u ON me.`user_id` = u.`id`\n" +
            "JOIN major AS m ON mep.`major_id` = m.`id`\n" +
            "WHERE YEAR(me.`update_time`) = 2024;")
    List<Map<String, Object>> selectAllInYear();
}