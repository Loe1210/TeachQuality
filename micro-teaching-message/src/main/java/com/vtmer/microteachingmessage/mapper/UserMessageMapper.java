package com.vtmer.microteachingmessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingmessage.pojo.UserMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Hung
 * @date 2022/5/25 10:35
 */
@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    @Select("select user_id from t_user_role where role_id =(select id from t_role where role_name = #{role});")
    List<Integer> getAllUsersByRole(@Param("role") String role);

    @Update("update notify_user set status = 1 where id = #{id}; ")
    Integer updateMessageStatus(@Param("id") Long id);

}
