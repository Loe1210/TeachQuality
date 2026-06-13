package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Hung
 * @date 2022/8/10 1:25
 */
@Mapper
public interface MajorMapper extends BaseMapper<Major> {
    int deleteByPrimaryKey(Integer id);

    int insert(Major record);

    int insertSelective(Major record);

    Major selectByPrimaryKey(Integer id);

    Major selectByName(String name);

    int updateByPrimaryKeySelective(Major record);

    int updateByPrimaryKey(Major record);

    List<Major> selectAllMajors(@Param("startIndex") Integer startIndex, @Param("length") Integer length);

    int selectCounts();

    Major selectByMajorName(@Param("majorName") String majorName);

    @Select("select `id` from major where `name` like CONCAT('%', #{majorName}, '%')")
    List<Integer> getMajorIds(String majorName);

    @Select("select `name` from major where `id` = #{id}")
    String getMajorName(int id);

    @Select("select `id` from major where `name` = #{name}")
    Integer getMajorIdByName(String name);
}