package com.vtmer.microteachingmessage.service.impl;

import com.vtmer.microteachingmessage.constant.ClazzEvaluationUserTypeConstant;
import com.vtmer.microteachingmessage.mapper.ClazzEvaluationProcessMapper;
import com.vtmer.microteachingmessage.mapper.UserMapper;
import com.vtmer.microteachingmessage.mapper.UserMessageMapper;
import com.vtmer.microteachingmessage.pojo.User;
import com.vtmer.microteachingmessage.pojo.UserMessage;
import com.vtmer.microteachingmessage.pojo.dto.ClazzEvaluationDTO;
import com.vtmer.microteachingmessage.pojo.dto.UserDTO;
import com.vtmer.microteachingmessage.pojo.dto.UserMessageDTO;
import com.vtmer.microteachingmessage.service.ClazzEvaluationProcessService;
import com.vtmer.microteachingmessage.service.UserMessageService;
import com.vtmer.microteachingmessage.util.UserUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hung
 * @date 2022/5/25 10:35
 */
@Service
public class ClazzEvaluationProcessServiceImpl implements ClazzEvaluationProcessService, ClazzEvaluationUserTypeConstant {
    @Resource
    private UserMessageMapper userMessageMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;
    @Autowired
    private UserMessageService userMessageService;


    @Override
    public <T, F> void evaluationProcessCreate(UserMessageDTO<T, F> userMessageDTO) {
        //课程评审流程创建，向SenderID（流程创建者）发生通知即可。
        String message = "您成功创建了课程评审流程，请您及时进入课程评审上传自评材料。";
        UserMessage userMessage = new UserMessage(1, 0, "系统", "提醒", (Long) userMessageDTO.getObjectId(),
                "课程评审流程", "", (Integer) userMessageDTO.getSenderId(), message);
        userMessageService.insertOneMessage(userMessage);
    }

    @SneakyThrows
    @Override
    public <T, F> void evaluationProcessPrincipalUpload(UserMessageDTO<T, F> userMessageDTO) {
        //课程负责人上传材料，向该类型所有课程评审专家评审
        //找出该用户的用户角色 根据角色类型来划分
        User user = userMapper.selectById((Long) userMessageDTO.getSenderId());
        List<String> userType = UserUtil.getUserType(user);

        //找出所有的专家，邀请专家进行评审
        List<Integer> informUsers = null;
        if (userType.contains(ARTS_AND_SCIENCES_EXPERT)) {
            informUsers = userMessageMapper.getAllUsersByRole(ARTS_AND_SCIENCES_EXPERT);
        } else if (userType.contains(ENGINEERING_EXPERT)) {
            informUsers = userMessageMapper.getAllUsersByRole(ENGINEERING_EXPERT);
        } else if (userType.contains(HUMANITIES_AND_SOCIAL_SCIENCES_EXPERT)) {
            informUsers = userMessageMapper.getAllUsersByRole(HUMANITIES_AND_SOCIAL_SCIENCES_EXPERT);
        }

        if (informUsers == null) {
            throw new NullPointerException("ClazzEvaluationProcessServiceImpl类 evaluationProcessPrincipalUpload方法 informUsers为空");
        }

        //获取发送用户的信息和课程信息
        UserDTO senderUserDTO = userMapper.selectUserName((Integer) userMessageDTO.getSenderId());
        ClazzEvaluationDTO clazzEvaluationInfo = clazzEvaluationProcessMapper.getClazzEvaluationInfo((Long) userMessageDTO.getObjectId());

        userMessageService.insertBatchMessage(informUsers.stream().map(userId -> {
            String message = clazzEvaluationInfo.getGrade() + clazzEvaluationInfo.getMajor() + "专业" + clazzEvaluationInfo.getName() + "课程的负责人" + senderUserDTO.getRealName() + "+已经上传自评材料，邀请您进行评审。";
            return new UserMessage(1, 0, "系统", "提醒", (Long) userMessageDTO.getObjectId(), "课程评审流程",
                    "", userId, message);
        }).collect(Collectors.toList()));
    }

    @Override
    public <T, F> void evaluationProcessSendBackMaterial(UserMessageDTO<T, F> userMessageDTO) {
        //找出 发送者和接收者
        UserDTO sender = userMapper.selectUserName((Integer) userMessageDTO.getSenderId());
        Integer receiverId = userMapper.selectUserId((Long) userMessageDTO.getObjectId());

        ClazzEvaluationDTO clazzEvaluationInfo = clazzEvaluationProcessMapper.getClazzEvaluationInfo((Long) userMessageDTO.getObjectId());

        String message = clazzEvaluationInfo.getName() + "课程的评审流程中" + sender.getRealName() + "评审专家对您的自评材料打回，请您重新上传材料";

        userMessageService.insertOneMessage(new UserMessage(1, 0, "系统", "提醒", (Long) userMessageDTO.getObjectId(), "课程评审流程",
                "", receiverId, message));

    }

    @Override
    public <T, F> void evaluationProcessExpertSubmit(UserMessageDTO<T, F> userMessageDTO) {
        //专家提交了评审，发送消息给专家组长
        User user = userMapper.selectById((Long) userMessageDTO.getSenderId());
        List<String> userType = UserUtil.getUserType(user);

        List<Integer> informUsers = null;
        if (userType.contains(ARTS_AND_SCIENCES_EXPERT_LEADER)) {
            informUsers = userMessageMapper.getAllUsersByRole(ARTS_AND_SCIENCES_EXPERT_LEADER);
        } else if (userType.contains(ENGINEERING_EXPERT_LEADER)) {
            informUsers = userMessageMapper.getAllUsersByRole(ENGINEERING_EXPERT_LEADER);
        } else if (userType.contains(HUMANITIES_AND_SOCIAL_SCIENCES_EXPERT_LEADER)) {
            informUsers = userMessageMapper.getAllUsersByRole(HUMANITIES_AND_SOCIAL_SCIENCES_EXPERT_LEADER);
        }

        if (informUsers == null) {
            throw new NullPointerException("ClazzEvaluationProcessServiceImpl类 evaluationProcessExpertSubmit方法 informUsers为空");
        }

        ClazzEvaluationDTO clazzEvaluationInfo = clazzEvaluationProcessMapper.getClazzEvaluationInfo((Long) userMessageDTO.getObjectId());

        userMessageService.insertBatchMessage(informUsers.stream().map(receiverId -> {
            String message = clazzEvaluationInfo.getGrade() + clazzEvaluationInfo.getMajor() + "专业" + clazzEvaluationInfo.getName() + "课程已经进行专家评审，邀请您进行评审。";
            return new UserMessage(1, 0, "系统", "提醒", (Long) userMessageDTO.getObjectId(), "课程评审流程",
                    "", receiverId, message);
        }).collect(Collectors.toList()));

    }

    @Override
    public <T, F> void evaluationProcessExpertLeaderSubmit(UserMessageDTO<T, F> userMessageDTO) {
        //专家组长提交了评审，发送消息给专家组长（给出小组评审意见）
        User user = userMapper.selectById((Long) userMessageDTO.getSenderId());
        List<String> userType = UserUtil.getUserType(user);

        List<Integer> informUsers = null;
        if (userType.contains(ARTS_AND_SCIENCES_EXPERT_LEADER)) {
            informUsers = userMessageMapper.getAllUsersByRole(ARTS_AND_SCIENCES_EXPERT_LEADER);
        } else if (userType.contains(ENGINEERING_EXPERT_LEADER)) {
            informUsers = userMessageMapper.getAllUsersByRole(ENGINEERING_EXPERT_LEADER);
        } else if (userType.contains(HUMANITIES_AND_SOCIAL_SCIENCES_EXPERT_LEADER)) {
            informUsers = userMessageMapper.getAllUsersByRole(HUMANITIES_AND_SOCIAL_SCIENCES_EXPERT_LEADER);
        }

        if (informUsers == null) {
            throw new NullPointerException("ClazzEvaluationProcessServiceImpl类 evaluationProcessExpertLeaderSubmit方法 informUsers为空");
        }

        ClazzEvaluationDTO clazzEvaluationInfo = clazzEvaluationProcessMapper.getClazzEvaluationInfo((Long) userMessageDTO.getObjectId());

        userMessageService.insertBatchMessage(informUsers.stream().map(receiverId -> {
            String message = clazzEvaluationInfo.getGrade() + clazzEvaluationInfo.getMajor() + "专业" + clazzEvaluationInfo.getName() + "课程已经进行专家组长评审，邀请您进行评审。";
            return new UserMessage(1, 0, "系统", "提醒", (Long) userMessageDTO.getObjectId(), "课程评审流程",
                    "", receiverId, message);
        }).collect(Collectors.toList()));
    }
}
