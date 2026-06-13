package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hung.microoauth2commons.commonutils.api.Result;
import com.vtmer.microteachingquality.model.pojo.Notify;
import com.vtmer.microteachingquality.model.vo.NotificationVO;

import java.util.List;

public interface NotifyService extends IService<Notify> {
    @Deprecated
    Boolean sendNotificationByClazzPrincipal(Integer userId);

    @Deprecated
    Boolean sendNotificationByClazzExpertLeader(Integer userId, Integer leaderId);

    @Deprecated
    Boolean sendNotificationByMajorLeader(Integer id);

    @Deprecated
    Boolean sendNotificationByMajorExpert(Integer id);

    Result<List<NotificationVO>> queryNotification(Integer pageNum);

    Boolean deleteOneMessage(Long id);

    Boolean deleteBatchMessage(List<Long> userMessageList);
}


