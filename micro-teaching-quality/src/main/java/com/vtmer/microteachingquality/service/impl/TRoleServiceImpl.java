package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.mapper.TRoleMapper;
import com.vtmer.microteachingquality.model.pojo.TRole;
import com.vtmer.microteachingquality.service.TRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : Gking
 * @date : 2022-07-20 15:26
 **/
@Service
public class TRoleServiceImpl extends ServiceImpl<TRoleMapper, TRole> implements TRoleService {
    @Autowired
    private TRoleMapper tRoleMapper;

    @Override
    public String getPermissionId(String permission) {
        QueryWrapper<TRole> wrapper = new QueryWrapper<>();
        QueryWrapper<TRole> role_name = wrapper.eq("role_name", permission);
        TRole tRole = tRoleMapper.selectOne(role_name);
        return tRole.getId();
    }
}
