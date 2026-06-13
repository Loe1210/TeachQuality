package com.vtmer.microteachingquality.service.impl;

import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.UserMapper;
import com.vtmer.microteachingquality.mapper.UserRoleMapper;
import com.vtmer.microteachingquality.model.bo.ReviseUserPermissionBO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.UserRole;
import com.vtmer.microteachingquality.service.PermissionVersionService;
import com.vtmer.microteachingquality.service.UserRoleService;
import com.vtmer.microteachingquality.util.UserUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author Hung
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private PermissionVersionService permissionVersionService;


    /**
     * 根据用户id获取角色
     *
     * @param userId@return
     */
    @Override
    public List<String> getUserRole(Integer userId) {
        return userRoleMapper.selectRoleById(userId);
    }

    @Override
    public boolean isPermissionHave(Integer userId, String permissionId, String ownerId) {
        Integer pId = Integer.valueOf(permissionId);
        //根据id查询对应的角色
        List<Integer> roleIds = userRoleMapper.selectRoleIdByUserIdAndCreatorId(userId, ownerId);
        //遍历，查询权限
        for (Integer roleId : roleIds) {
            if (Objects.equals(roleId, pId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCaptainPermissionHave(Integer userId) {
        //根据id查询对应的角色
        List<String> roleNames = userRoleMapper.selectRoleNameByUserId(userId);
        for (String roleName : roleNames) {
            if (Objects.equals(roleName, "专家评审专家组长")
                    || Objects.equals(roleName, "课程评审专家组长")
            ) {
                return true;
            }
        }
        return false;

    }

    /**
     * @param reviseUserPermissionBO
     * @return
     */
    @Override
    public boolean reviseUserRole(ReviseUserPermissionBO reviseUserPermissionBO) {
        //检查集合长度
        if (reviseUserPermissionBO.getRoles().size() != 3) {
            throw new CustomException("缺少用户角色，请重试");
        }

        if (userRoleMapper.deleteUserRoleById(reviseUserPermissionBO.getUserId()) < 0) {

            throw new CustomException();
        }

        reviseUserPermissionBO.getRoles().forEach(roleName -> {
            User user = new User();
            user.setRoles(userRoleService.getUserRole(reviseUserPermissionBO.getUserId()));
            int roleId;
            if (UserUtil.isTestRole(user)) {
                roleId = userRoleMapper.getRoleIdByName("测试" + roleName);
            } else {
                roleId = userRoleMapper.getRoleIdByName(roleName);
            }
            new UserRole(reviseUserPermissionBO.getUserId(), roleId).insert();
        });
        permissionVersionService.bump(reviseUserPermissionBO.getUserId());

        return true;
    }

}
