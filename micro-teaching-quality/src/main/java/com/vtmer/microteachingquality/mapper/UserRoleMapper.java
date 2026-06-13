package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.UserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Hung
 * @date 2022/11/3 23:44
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    @Delete("delete from t_user_role where user_id= #{id};")
    int deleteByPrimaryKey(Integer id);

    @Insert("insert into t_user_role(user_id,role_id) values (#{userId},#{roleId});")
    int insertRecord(UserRole record);

    @Select("select id,user_id,role_id from t_user_role where id = #{id}")
    UserRole selectByPrimaryKey(Integer id);

    @Select("select id,user_id,role_id from t_user_role;")
    List<UserRole> selectAll();

    @Update("update t_user_role set user_id=#{userId},role_id=#{roleId} where user_id =#{id}")
    int updateByPrimaryKey(UserRole record);

    @Delete("delete from t_user_role where user_id = #{userId};")
    int deleteUserRoleById(Integer userId);

    @Select("select id from t_role where role_name=#{roleName};")
    int getRoleIdByName(String roleName);

    @Update("update t_user_role set user_id=#{userId},role_id=#{newRow} where user_id =#{id} and role_id = #{roleId}")
    int updateByPrimaryKeyAndNewRole(UserRole record, int newRow);

    /**
     * 根据用户id查询角色id
     *
     * @param userId
     * @return
     */
    @Select("select role_id from t_user_role where user_id=#{userId};")
    List<Integer> selectRoleIdByUserId(Integer userId);

    /**
     * 根据用户id以及creatorID 查询角色id
     *
     * @param userId
     * @return
     */
    @Select("select role_id from t_user_role where user_id=#{userId},creator=#{creatorId};")
    List<Integer> selectRoleIdByUserIdAndCreatorId(Integer userId, String creatorId);

    /**
     * 根据用户id查询角色名称
     *
     * @param userId
     * @return
     */
    @Select("select role_name from t_user_role where user_id=#{userId};")
    List<String> selectRoleNameByUserId(Integer userId);


    @Select("SELECT r.role_name FROM t_role r, t_user_role u WHERE  r.id=u.role_id AND u.user_id = #{userId}; ")
    List<String> selectRoleById(Integer userId);
}
