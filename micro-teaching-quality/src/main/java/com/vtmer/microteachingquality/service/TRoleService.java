package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.pojo.TRole;

public interface TRoleService extends IService<TRole> {
    String getPermissionId(String permission);
}
