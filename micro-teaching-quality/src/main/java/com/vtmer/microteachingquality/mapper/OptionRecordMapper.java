package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.OptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OptionRecordMapper extends BaseMapper<OptionRecord> {
    int deleteByPrimaryKey(Integer id);

    int insert(OptionRecord record);

    OptionRecord selectByPrimaryKey(Integer id);

    List<OptionRecord> selectAll();

    int updateByPrimaryKey(OptionRecord record);

    /**
     * 根据用户id查询评审选项记录
     *
     * @param userId
     * @return
     */
    List<OptionRecord> selectByUserId(Integer userId);

    /**
     * 根据用户id和专业id查询评审选项记录
     *
     * @param majorId
     * @param userId
     * @return
     */
    @Select("select o.id,\n" +
            "               o.user_id,\n" +
            "               o.major_id,\n" +
            "               o.option_id,\n" +
            "               o.mark,\n" +
            "               o.create_time,\n" +
            "               o.update_time\n" +
            "        from option_record o where user_id= #{userId} and major_id = #{majorId} ; ")
    List<OptionRecord> selectByUserIdAndMajorId(@Param("userId") Integer userId, @Param("majorId") Integer majorId);
}