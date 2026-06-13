package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.Clazz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClazzMapper extends BaseMapper<Clazz> {

    int insert(Clazz record);

    @Select("select `id`,`college`,`major`,`name`,`grade`,`user_id`,`clazz_serial_number`,`type`,`create_time`,`update_time` from clazz where id = #{id};")
    Clazz selectByPrimaryKey(@Param("id") Integer id);

    @Select("select `id`,`college`,`major`,`name`,`grade`,`user_id`,`clazz_serial_number`,`type`,`create_time`,`update_time` from clazz where 'name'=#{name};")
    Clazz selectByName(@Param("name") String name);

    @Select("select `id`,`college`,`major`,`name`,`user_id`,`clazz_serial_number`,`type`,`create_time`,`update_time` from clazz; ")
    List<Clazz> selectAll();

    int insertEmptyData(String tableName);

    @Select("select id from clazz where college= #{college} and major=#{major} and type=#{clazzType} and name=#{clazzName};")
    Integer selectClazzId(@Param("college") String college, @Param("major") String major, @Param("clazzType") String clazzType, @Param("clazzName") String clazzName);

    @Select("SELECT distinct c.* " +
            "FROM clazz_file cf " +
            "JOIN class_evaluation_process cep ON cf.clazz_evaluation_process_id = cep.evaluation_id " +
            "JOIN Clazz c ON cep.clazz_id = c.id " +
            "WHERE cf.clazz_evaluation_process_id = #{clazzEvaluationProcessId}")
    Clazz selectClazzByClazzFileName(@Param("clazzEvaluationProcessId") Long clazzEvaluationProcessId);

    @Select("select `id` from clazz where `name` like CONCAT('%', #{clazzName}, '%')")
    List<Integer> getClazzIds(String clazzName);

    @Select("select `name` from clazz where `id` = #{id}")
    String getClazzName(int id);

    @Select("select `id` from clazz where `name` = #{name}")
    Integer getClazzIdByName(String name);
}