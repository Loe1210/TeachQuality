package com.vtmer.microteachingquality.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.mapper.MajorEvaluationFileMapper;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationFile;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationProcess;
import com.vtmer.microteachingquality.service.MajorEvaluationFileService;
import com.vtmer.microteachingquality.util.UserUtil;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus.END;
import static com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus.UNDERWAY;
import static com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant.MASTER;

/**
 * @author Colin_Knight
 * @create 2023/5/8 16:30
 */
@Service
public class MajorEvaluationFileServiceImpl extends ServiceImpl<MajorEvaluationFileMapper, MajorEvaluationFile> implements MajorEvaluationFileService {

    @Autowired
    private MajorEvaluationFileMapper majorEvaluationFileMapper;


    @Override
    public MajorEvaluationFile getMajorEvaluationFile(String path) {
        QueryWrapper<MajorEvaluationFile> clazzFileQueryWrapper = new QueryWrapper<>();
        clazzFileQueryWrapper.eq("path", path);
        return majorEvaluationFileMapper.selectOne(clazzFileQueryWrapper);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean storePrincipalMajorEvaluationFile(User user, MultipartFile file, Long evaluationProcessId, Integer majorId, String filePath, String encryptPath) throws MQBrokerException, RemotingException, InterruptedException, MQClientException, IOException {
        // 存储课程报告上传信息(修改课程报告上传者、文件路径)
        MajorEvaluationFile majorEvaluationFileInsert = new MajorEvaluationFile(user.getId(), file.getOriginalFilename(), majorId, evaluationProcessId, encryptPath);

        //更改课程评审流程状态
        UpdateWrapper<MajorEvaluationProcess> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("principal_material_status", END);
        updateWrapper.eq("expert_review_status", UNDERWAY);
        updateWrapper.eq("evaluation_id", evaluationProcessId);
        MajorEvaluationProcess majorEvaluationProcess = new MajorEvaluationProcess();
        majorEvaluationProcess.update(updateWrapper);

        if (majorEvaluationFileInsert.insert()) {
            //rocketMQTemplate.getProducer().send(new Message(ClazzEvaluationTopic.CLAZZ_EVALUATION, ClazzEvaluationTopic.PRINCIPAL_UPLOAD, UserMessageDTO.newInstance(new UserMessageDTO<>(user.getId(), evaluationProcessId))));
            FileUtil.writeBytes(file.getBytes(), filePath);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteMajorFileRecord(String path) {
        QueryWrapper<MajorEvaluationFile> wrapper = new QueryWrapper<>();
        wrapper.eq("path", path);
        MajorEvaluationFile file = majorEvaluationFileMapper.selectOne(wrapper);


        if (file.getUserId().equals(UserUtil.getCurrentUser().getId()) || UserUtil.isRoleAvailable(MASTER)) {
            return majorEvaluationFileMapper.deleteById(file.getId()) == 1 ? "删除成功" : "删除失败";
        }
        return "只有管理员和上传者有删除权限";
    }

    @Override
    public MajorEvaluationFile getMajorEvaluationFileByPath(String path) {
        QueryWrapper<MajorEvaluationFile> wrapper = new QueryWrapper<MajorEvaluationFile>().eq("path", path);
        return majorEvaluationFileMapper.selectOne(wrapper);
    }
}
