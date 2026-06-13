package com.vtmer.microteachingquality.controller;

import com.hung.microoauth2commons.commonutils.api.Result;
import com.vtmer.microteachingquality.model.vo.NotificationVO;
import com.vtmer.microteachingquality.service.NotifyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : Gking
 * @date : 2022-04-24 15:15
 **/
@Api(tags = "获取消息通知相关接口")
@RestController
@RequestMapping("/notify")
@Slf4j
public class NotifyController {

    @Autowired
    private NotifyService notifyService;


    @ApiOperation("获取通知  每次返回5条")
    @GetMapping("/notification")
    public Result<List<NotificationVO>> getNotification(Integer pageNum) {
        return notifyService.queryNotification(pageNum);
    }

    @ApiOperation("忽略通知")
    @PutMapping("/notification")
    public Result<String> ignoreNotification(Long notificationId) {
        return notifyService.deleteOneMessage(notificationId) ? Result.success("") : Result.failed("");
    }

    @ApiOperation("忽略批量通知")
    @PutMapping("/notification/batch")
    public Result<String> ignoreNotification(List<Long> notificationIds) {
        return notifyService.deleteBatchMessage(notificationIds) ? Result.success("") : Result.failed("");
    }

}
