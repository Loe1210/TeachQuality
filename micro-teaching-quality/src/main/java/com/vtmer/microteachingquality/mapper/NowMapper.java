package com.vtmer.microteachingquality.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface NowMapper {

    @Select("SELECT ce.`evaluation_id`, c.`name`, cor.`evaluation_opinion`, cor.`remark`, c.`college`\n" +
            "FROM `user` AS u\n" +
            "JOIN `clazz_opinion_leader_record` AS cor ON u.`id` = cor.`user_id` AND YEAR(cor.`update_time`) = 2024\n" +
            "JOIN `class_evaluation_process` AS ce ON cor.`clazz_evaluation_process_id` = ce.`evaluation_id`\n" +
            "JOIN `clazz` AS c ON c.`id` = ce.`clazz_id`\n" +
            "WHERE u.`real_name` = #{name};")
    List<Map<String, Object>> getDetailsByName(String name);
}
