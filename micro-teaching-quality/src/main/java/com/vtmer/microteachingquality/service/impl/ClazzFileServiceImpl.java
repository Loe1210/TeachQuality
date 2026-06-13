package com.vtmer.microteachingquality.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.mapper.ClazzFileMapper;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzFile;
import com.vtmer.microteachingquality.service.ClazzEvaluationProcessService;
import com.vtmer.microteachingquality.service.ClazzFileService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus.END;
import static com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus.UNDERWAY;

/**
 * @author Hung
 * @date 2022/4/22 0:23
 */
@Service
public class ClazzFileServiceImpl extends ServiceImpl<ClazzFileMapper, ClazzFile> implements ClazzFileService {

    @Autowired
    private ClazzFileMapper clazzFileMapper;
    @Autowired
    private ClazzEvaluationProcessService clazzEvaluationProcessService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public ClazzFile getClazzFile(String path) {
        QueryWrapper<ClazzFile> clazzFileQueryWrapper = new QueryWrapper<>();
        clazzFileQueryWrapper.eq("path", path);
        return clazzFileMapper.selectOne(clazzFileQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean storePrincipalClazzFile(User user, MultipartFile file, Long evaluationProcessId, Integer clazzId, String filePath, String encryptPath) throws MQBrokerException, RemotingException, InterruptedException, MQClientException, IOException {
        // 存储课程报告上传信息(修改课程报告上传者、文件路径)
        ClazzFile clazzFileInsert = new ClazzFile(user.getId(), file.getOriginalFilename(), clazzId, evaluationProcessId, encryptPath);

        //更改课程评审流程状态
        UpdateWrapper<ClassEvaluationProcess> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("principal_material_status", END);
        updateWrapper.eq("expert_review_status", UNDERWAY);
        updateWrapper.eq("evaluation_id", evaluationProcessId);
        ClassEvaluationProcess classEvaluationProcess = new ClassEvaluationProcess();
        classEvaluationProcess.update(updateWrapper);

        if (clazzFileInsert.insert()) {
            //rocketMQTemplate.getProducer().send(new Message(ClazzEvaluationTopic.CLAZZ_EVALUATION, ClazzEvaluationTopic.PRINCIPAL_UPLOAD, UserMessageDTO.newInstance(new UserMessageDTO<>(user.getId(), evaluationProcessId))));
            FileUtil.writeBytes(file.getBytes(), filePath);
            return true;
        }
        return false;
    }

    /**
     * @param fileId
     * @return
     */
    @Override
    public Boolean deleteClazzFileRecord(Integer fileId) {
        return clazzFileMapper.deleteClazzFileRecord(fileId);
    }
}
