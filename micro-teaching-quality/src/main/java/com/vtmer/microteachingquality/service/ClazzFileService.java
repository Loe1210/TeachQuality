package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzFile;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Hung
 * @date 2022/4/22 0:22
 */
public interface ClazzFileService extends IService<ClazzFile> {
    ClazzFile getClazzFile(String path);

    /**
     * 存储课程评审负责人上传的自评报告
     *
     * @return 是否上传成功
     */
    Boolean storePrincipalClazzFile(User user, MultipartFile file, Long clazzEvaluationProcessId, Integer clazzId, String filePath, String encryptPath) throws MQBrokerException, RemotingException, InterruptedException, MQClientException, IOException;


    Boolean deleteClazzFileRecord(Integer fileId);
}
