package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationFile;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Colin_Knight
 * @create 2023/5/8 16:29
 */
public interface MajorEvaluationFileService extends IService<MajorEvaluationFile> {

    MajorEvaluationFile getMajorEvaluationFile(String path);

    /**
     * 存储课程评审负责人上传的自评报告
     *
     * @return 是否上传成功
     */
    Boolean storePrincipalMajorEvaluationFile(User user, MultipartFile file, Long clazzEvaluationProcessId, Integer clazzId, String filePath, String encryptPath) throws MQBrokerException, RemotingException, InterruptedException, MQClientException, IOException;


    String deleteMajorFileRecord(String path);

    MajorEvaluationFile getMajorEvaluationFileByPath(String path);

}
