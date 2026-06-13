package com.vtmer.microteachingmessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingmessage.pojo.User;
import com.vtmer.microteachingmessage.pojo.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Hung
 * @date 2022/5/25 19:50
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 获取userId
     *
     * @param userId 用户id
     * @return 用户DTO
     */
    @Select("select user_id,real_name from user where user_id=#{userId};")
    UserDTO selectUserName(@Param("userId") Integer userId);


    @Select("select creator_id from evaluation_process where evaluation=#{id};")
    Integer selectUserId(@Param("id") Long clazzEvaluationProcessId);
}
