package com.vtmer.microteachingquality.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.bo.*;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.*;
import com.vtmer.microteachingquality.model.vo.FinishedReviewVO;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationGetFileResult;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationProcessInfo;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationProcessSimpleInfoVo;
import com.vtmer.microteachingquality.service.MajorEvaluationFileService;
import com.vtmer.microteachingquality.service.MajorEvaluationProcessService;
import com.vtmer.microteachingquality.util.UserUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus.*;

/**
 * @author Hung
 * @date 2022/8/10 16:07
 */
@Service
@Slf4j
public class MajorEvaluationProcessServiceImpl implements MajorEvaluationProcessService, UserTypeConstant {

    /**
     * 随机生成存储加密文件名(自评报告)的密钥
     */
    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);
    @Resource
    private MajorEvaluationProcessMapper majorEvaluationProcessMapper;
    @Resource
    private MajorMapper majorMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private MasterEvaluationMapper masterEvaluationMapper;
    @Resource
    private LeaderEvaluationMapper leaderEvaluationMapper;
    @Value("${report.template.path}")
    private String reportTemplatePath;
    @Value("${report.path}")
    private String reportPath;

    @Resource
    private MajorEvaluationFileMapper majorEvaluationFileMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MajorEvaluationFileService majorEvaluationFileService;


    @SneakyThrows
    @Override
    public Boolean createEvaluationProcess(Integer majorId) {
        //判断专业是否存在
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new CustomException("专业不存在");
        }

        User user = UserUtil.getCurrentUser();
        //提前生成id，保持一致性
        Long processId = IdUtil.getSnowflake(0, 0).nextId();
        MajorEvaluationProcess majorEvaluationProcess = new MajorEvaluationProcess(processId, majorId, user.getId(), LocalDateTime.now().getYear() + "级");
        if (majorEvaluationProcess.insert()) {
            //rocketMQTemplate.getProducer().send(new Message(MajorEvaluationTopic.MAJOR_EVALUATION, MajorEvaluationTopic.PROCESS_CREATED, new UserMessageDTO<>(user.getId(), majorEvaluationProcessId).toString().getBytes()));
            log.info("用户id {} {} 创建专业评审流程成功，流程id：{}", user.getId(), user.getRealName(), processId);
            return true;

        }
        log.info("用户id {} {} 创建专业评审流程失败", user.getId(), user.getRealName());
        return false;

    }

    @Override
    public String deleteEvaluationProcess(Long majorEvaluationProcessId) {
        User loginUser = UserUtil.getCurrentUser();
        MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectById(majorEvaluationProcessId);

        if (majorEvaluationProcess.getCreatorId().equals(loginUser.getId()) || UserUtil.isRoleAvailable(MASTER)) {
            majorEvaluationProcessMapper.deleteById(majorEvaluationProcessId);
            return "删除成功";
        }

        return "只有管理员和创建者有删除权限";
    }

    @Override
    public List<MajorEvaluationProcessSimpleInfoVo> getEvaluationProcesses(SelectMajorEvaluationListBO majorEvaluationListBO) {
        //判断专业是否存在
        Major major = majorMapper.selectById(majorEvaluationListBO.getMajorId());
        if (major == null) {
            throw new CustomException("专业不存在");
        }

        QueryWrapper<MajorEvaluationProcess> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", majorEvaluationListBO.getMajorId());
        // 修改为显示2024年10月之后的数据
        //管理员不加时间限制
        if (!UserUtil.isRoleAvailable(MASTER)) {
            queryWrapper.apply("(YEAR(create_time) > 2024 OR (YEAR(create_time) = 2024 AND MONTH(create_time) > 10))");
        }
        queryWrapper.orderByDesc("create_time");
        List<MajorEvaluationProcess> majorEvaluationProcessList = majorEvaluationProcessMapper.selectPage(new Page<>(majorEvaluationListBO.getPageNum(), majorEvaluationListBO.getPageSize()), queryWrapper).getRecords();

        if (majorEvaluationProcessList.isEmpty()) {
            return new ArrayList<>();
        }

        return majorEvaluationProcessList.stream().map(majorEvaluationProcess -> {
            MajorEvaluationProcessSimpleInfoVo majorEvaluationProcessSimpleInfoVo = new MajorEvaluationProcessSimpleInfoVo();
            BeanUtils.copyProperties(majorEvaluationProcess, majorEvaluationProcessSimpleInfoVo);

            majorEvaluationProcessSimpleInfoVo.setId(Long.toString(majorEvaluationProcess.getId()));
            majorEvaluationProcessSimpleInfoVo.setCreatorName(userMapper.selectById(majorEvaluationProcess.getCreatorId()).getRealName());

            if (majorEvaluationProcess.getProcessEndStatus() == 2) {
                majorEvaluationProcessSimpleInfoVo.setStatus("流程结束");
                return majorEvaluationProcessSimpleInfoVo;
            }

            if (majorEvaluationProcess.getExpertLeaderReviewStatus() == 2) {
                majorEvaluationProcessSimpleInfoVo.setStatus("专家组长评审完成");
                return majorEvaluationProcessSimpleInfoVo;
            }

            if (majorEvaluationProcess.getExpertReviewStatus() == 2) {
                majorEvaluationProcessSimpleInfoVo.setStatus("专家评审完成");
                return majorEvaluationProcessSimpleInfoVo;
            }


            if (majorEvaluationProcess.getPrincipalMaterialStatus() == 2) {
                majorEvaluationProcessSimpleInfoVo.setStatus("负责人完成上传材料");
                return majorEvaluationProcessSimpleInfoVo;
            }

            if (majorEvaluationProcess.getPrincipalMaterialStatus() == 1) {
                majorEvaluationProcessSimpleInfoVo.setStatus("等待负责人上传材料");
                return majorEvaluationProcessSimpleInfoVo;
            }

            if (majorEvaluationProcess.getDeanReviewStatus() == 1) {
                majorEvaluationProcessSimpleInfoVo.setStatus("等待教学院长审核");
            }

            if (majorEvaluationProcess.getDeanReviewStatus() == 2) {
                majorEvaluationProcessSimpleInfoVo.setStatus("教学院长完成审核");
            }

            return majorEvaluationProcessSimpleInfoVo;
        }).collect(Collectors.toList());
    }

    @Override
    public String getMajorTypeByProcessId(Long majorEvaluationProcessId) {
        MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectById(majorEvaluationProcessId);
        Major major = majorMapper.selectById(majorEvaluationProcess.getMajorId());
        return major.getType();
    }

    @Override
    public Boolean saveDeanEvaluation(DeanEvaluationBO deanEvaluationBO) {
        Integer pass = deanEvaluationBO.getPass();

        MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectById(deanEvaluationBO.getMajorEvaluationProcessId());

        if (pass == 0) {
            // 不通过
            majorEvaluationProcess.setPrincipalMaterialStatus(UNDERWAY);
            majorEvaluationProcess.setDeanReviewStatus(0);
            majorEvaluationProcess.setExpertReviewStatus(0);
            majorEvaluationProcessMapper.updateById(majorEvaluationProcess);
            return false;
        } else {
            // 通过
            majorEvaluationProcess.setDeanReviewStatus(END);
            majorEvaluationProcess.setExpertReviewStatus(UNDERWAY);
            majorEvaluationProcessMapper.updateById(majorEvaluationProcess);
            return true;
        }
    }


    @SneakyThrows
    @Override
    public boolean principalUploadMaterial(MultipartFile file, Long majorEvaluationProcessId) {
        // 获取当前登陆用户(专业负责人)对象
        User user = UserUtil.getCurrentUser();

        if (StrUtil.isBlank(file.getOriginalFilename())) {
            throw new CustomException("请选择需要上传的文件");
        }

        //判断评审流程是否存在
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(majorEvaluationProcessId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        // 目录添加uuid防重
        String uuid = IdUtil.simpleUUID();
        File path = new File(reportPath + File.separator + uuid);
        if (!path.isDirectory()) {
            path.mkdirs();
        }

        //生成文件路径
        String filePath = reportPath + File.separator + uuid + File.separator + file.getOriginalFilename();

        // 加密文件名
        String encryptFileName = aes.encryptHex(uuid + File.separator + file.getOriginalFilename());

        // String filePath = reportPath + File.separator + encryptFileName;

        // 存储自评报告上传信息(修改自评报告上传者、状态、文件路径)

        long count = majorEvaluationFileService.count(new QueryWrapper<MajorEvaluationFile>().eq("user_id", user.getId()).eq("major_evaluation_process_id", majorEvaluationProcessId).eq("file_name", file.getOriginalFilename()));
        if (count != 0L) {
            throw new CustomException("上传专业自评报告失败,已存在同名文件");
        }

        Report report = new Report(user.getId(), process.getMajorId(), majorEvaluationProcessId, file.getOriginalFilename(), encryptFileName);
        //TODO 这里原来的path为encryptFileName，后续可能需要更改

        //存储专业报告

        Integer majorId = majorEvaluationProcessMapper.selectById(majorEvaluationProcessId).getMajorId();

        MajorEvaluationFile majorEvaluationFile = new MajorEvaluationFile(
                majorId,
                user.getId(),
                majorEvaluationProcessId,
                file.getOriginalFilename(),
                encryptFileName
        );


        if (majorEvaluationFile.insert()) {
            //rocketMQTemplate.getProducer().send(new Message(MajorEvaluationTopic.MAJOR_EVALUATION, MajorEvaluationTopic.PRINCIPAL_UPLOAD, new UserMessageDTO<>(user.getId(), majorEvaluationProcessId).toString().getBytes()));
            FileUtil.writeBytes(file.getBytes(), filePath);

            UpdateWrapper<MajorEvaluationProcess> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("principal_material_status", END);
            updateWrapper.set("dean_review_status", UNDERWAY);
//            updateWrapper.set("expert_review_status", UNDERWAY);
            updateWrapper.eq("id", process.getId());
            process.update(updateWrapper);

            log.info("用户id {} {} 在专业评审上传自评报告成功，专业评审流程id：{}", user.getId(), user.getRealName(), majorEvaluationProcessId);
            return true;
        }
        log.info("用户id {} {} 在专业评审上传自评报告失败，专业评审流程id：{}", user.getId(), user.getRealName(), majorEvaluationProcessId);
        throw new CustomException("上传自评报告失败");
    }

    @Override
    public List<MajorEvaluationGetFileResult> getEvaluationMaterialInformation(Long majorEvaluationProcessId) {

        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(majorEvaluationProcessId);

        if (process == null) {
            throw new CustomException("评审流程不存在");
        }


        QueryWrapper<MajorEvaluationFile> majorEvaluationQueryWrapper = new QueryWrapper<>();
        majorEvaluationQueryWrapper.select("id", "user_id", "file_name", "path");
        majorEvaluationQueryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId);

        List<MajorEvaluationFile> majorEvaluationFiles = majorEvaluationFileMapper.selectList(majorEvaluationQueryWrapper);

        //返回文件数据
//        List<MajorEvaluationProcess> majorEvaluationProcesses = majorEvaluationProcessMapper.selectList(majorEvaluationQueryWrapper);
        return majorEvaluationFiles.stream().map(majorEvaluationProcess -> {
            MajorEvaluationGetFileResult majorEvaluationGetFileResult = new MajorEvaluationGetFileResult();
            BeanUtils.copyProperties(majorEvaluationProcess, majorEvaluationGetFileResult);
            return majorEvaluationGetFileResult;
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMasterEvaluation(MasterEvaluateBO masterEvaluateBO) {
        //判断评审流程是否存在
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(masterEvaluateBO.getMajorEvaluationProcessId());
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }
        User currentUser = UserUtil.getCurrentUser();
        Long majorEvaluationProcessId = Long.valueOf(masterEvaluateBO.getMajorEvaluationProcessId());
        validateOptionMap(masterEvaluateBO.getOptionMap());
        if (getMasterEvaluation(majorEvaluationProcessId, currentUser.getId()) != null) {
            throw new CustomException("当前已提交评审，请使用更新接口");
        }
        persistMasterEvaluation(masterEvaluateBO, process, currentUser.getId(), false);
        //rocketMQTemplate.getProducer().send(new Message(MajorEvaluationTopic.MAJOR_EVALUATION, MajorEvaluationTopic.EXPERT_SUBMIT, new UserMessageDTO<>(currentUser.getId(), masterEvaluateBO.getMajorEvaluationProcessId()).toString().getBytes()));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveLeaderEvaluation(LeaderEvaluateBO leaderEvaluateBO) {
        User user = UserUtil.getCurrentUser();

        //判断评审流程是否存在
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(leaderEvaluateBO.getMajorEvaluationProcessId());
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }
        if (!END.equals(process.getExpertReviewStatus())) {
            throw new CustomException("当前未到专家组长评审阶段");
        }
        validateOptionMap(leaderEvaluateBO.getOptionMap());

        // 检测是否已填写
        QueryWrapper<LeaderEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_evaluation_process_id", leaderEvaluateBO.getMajorEvaluationProcessId());
        queryWrapper.eq("user_id", user.getId());
        LeaderEvaluation leaderEvaluationSelect = leaderEvaluationMapper.selectOne(queryWrapper);
        if (leaderEvaluationSelect != null) {
            throw new CustomException("已经填写过评审表了");
        }

        //选项表插入
        Long majorEvaluationProcessId = Long.valueOf(leaderEvaluateBO.getMajorEvaluationProcessId());
        Map<Integer, String> optionMap = leaderEvaluateBO.getOptionMap();
        for (Map.Entry<Integer, String> option : optionMap.entrySet()) {
            OptionRecord optionRecord = new OptionRecord(user.getId(), majorEvaluationProcessId, option.getKey(), option.getValue());
            optionRecord.insert();
        }

        //评审表插入
        LeaderEvaluation leaderEvaluation = new LeaderEvaluation(user.getId(), majorEvaluationProcessId, leaderEvaluateBO.getResult(), leaderEvaluateBO.getOpinion());
        if (leaderEvaluation.insert()) {
            //更新专业评审的状态，将结果设置为Result
            log.info("开始改状态");
            UpdateWrapper<MajorEvaluationProcess> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("remark", leaderEvaluateBO.getResult());
            updateWrapper.set("expert_leader_review_status", END);
            updateWrapper.set("process_end_status", UNDERWAY);
            updateWrapper.eq("id", process.getId());
            process.update(updateWrapper);
            log.info("修改状态完成");
            return true;
        }

        throw new CustomException("专家组长评审 保存失败");
    }

    @SneakyThrows
    @Override
    public boolean sendBackEvaluation(Long evaluationProcessId, Integer userId) {
        //判断评审流程是否存在
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(evaluationProcessId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }
        MasterEvaluation masterEvaluation = getMasterEvaluation(evaluationProcessId, userId);
        if (masterEvaluation == null) {
            throw new CustomException("未查到该专家评审记录");
        }
        masterEvaluation.setStatus(1);
        if (masterEvaluationMapper.updateById(masterEvaluation) <= 0) {
            throw new CustomException("退回评审失败");
        }

        //更新流程状态
        UpdateWrapper<MajorEvaluationProcess> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("expert_review_status", UNDERWAY);
        updateWrapper.set("expert_leader_review_status", 0);
        updateWrapper.eq("id", process.getId());
        process.update(updateWrapper);

        //rocketMQTemplate.getProducer().send(new Message(MajorEvaluationTopic.MAJOR_EVALUATION, MajorEvaluationTopic.MATERIAL_BACK, new UserMessageDTO<>(userId, evaluationProcessId).toString().getBytes()));

        return true;
    }

    @Override
    public MajorEvaluationProcessInfo getEvaluationProcessInfo(Long majorEvaluationId) {
        //获取评审流程基本信息
        MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectById(majorEvaluationId);
        if (majorEvaluationProcess == null) {
            throw new CustomException("评审流程不存在");
        }

        MajorEvaluationProcessInfo majorEvaluationProcessInfo = new MajorEvaluationProcessInfo();
        BeanUtils.copyProperties(majorEvaluationProcess, majorEvaluationProcessInfo);
        User user = userMapper.selectById(majorEvaluationProcess.getCreatorId());
        majorEvaluationProcessInfo.setCreatorName(user.getRealName());

        //根据当前角色来决定是否有相应权限
        User currentUser = UserUtil.getCurrentUser();


        //判断此用户是否已经参与评审了
        QueryWrapper<MasterEvaluation> masterEvaluationQueryWrapper = new QueryWrapper<>();
        masterEvaluationQueryWrapper.eq("major_evaluation_process_id", majorEvaluationId);
        masterEvaluationQueryWrapper.eq("user_id", currentUser.getId());

        MasterEvaluation masterEvaluation = masterEvaluationMapper.selectOne(masterEvaluationQueryWrapper);
        if (masterEvaluation != null) {
            majorEvaluationProcessInfo.setIsReview(true);
        }

        //判断是不是评审流程创建者
        if (majorEvaluationProcess.getCreatorId().equals(currentUser.getId())) {
            majorEvaluationProcessInfo.setFileReviseAuthority(1);
//            return majorEvaluationProcessInfo;
        }

        log.info("用户的身份是：{}", currentUser);

        //判断是评审专家还是评审专家组长
        if (UserUtil.isRoleAvailable(MAJOR_EVALUATION_EXPERT) || UserUtil.isRoleAvailable(TEST_MAJOR_EVALUATION_EXPERT)) {
            log.info("专业评审专家");
            majorEvaluationProcessInfo.setReviewAuthority(1);
            majorEvaluationProcessInfo.setReviewInformationAuthority(1);
            return majorEvaluationProcessInfo;
        }

        if (UserUtil.isRoleAvailable(MAJOR_EVALUATION_PRINCIPAL_LEADER) || UserUtil.isRoleAvailable(TEST_MAJOR_EVALUATION_PRINCIPAL_LEADER)) {
            log.info("专业评审专家组长");
            majorEvaluationProcessInfo.setReviewLeaderAuthority(1);
            majorEvaluationProcessInfo.setReviewInformationAuthority(2);
            return majorEvaluationProcessInfo;
        }

        if (UserUtil.isRoleAvailable(MASTER)) {
            majorEvaluationProcessInfo.setFileReviseAuthority(1);
        }


        return majorEvaluationProcessInfo;
    }

    @Override
    public List<FinishedReviewVO> getAllFinishedReviews(Long evaluationProcessId) {
        //判断评审流程是否存在
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(evaluationProcessId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }


        QueryWrapper<MasterEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_evaluation_process_id", evaluationProcessId);


        List<MasterEvaluation> reviewInfo = masterEvaluationMapper.selectList(queryWrapper);


        return reviewInfo.stream().map(masterEvaluation -> {
            User user = new User().selectById(masterEvaluation.getUserId());
            return new FinishedReviewVO(user.getId(), user.getRealName(), masterEvaluation.getUpdateTime());
        }).collect(Collectors.toList());
    }

    @Override
    public List<FinishedReviewVO> getLeaderFinishedReviews(Long evaluationProcessId) {
        //判断评审流程是否存在
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(evaluationProcessId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }


        QueryWrapper<LeaderEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_evaluation_process_id", evaluationProcessId);


        List<LeaderEvaluation> leaderEvaluations = leaderEvaluationMapper.selectList(queryWrapper);


        return leaderEvaluations.stream().map(leaderEvaluation -> {
            User user = new User().selectById(leaderEvaluation.getUserId());
            return new FinishedReviewVO(user.getId(), user.getRealName(), leaderEvaluation.getUpdateTime());
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMasterEvaluation(MasterEvaluateBO masterEvaluateBO) {
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(masterEvaluateBO.getMajorEvaluationProcessId());
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }
        User currentUser = UserUtil.getCurrentUser();
        validateOptionMap(masterEvaluateBO.getOptionMap());
        persistMasterEvaluation(masterEvaluateBO, process, currentUser.getId(), true);
        return true;
    }

    @Override
    public boolean endEvaluationProcess(EndEvaluationProcessBO endEvaluationProcessBO) {
        MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectById(endEvaluationProcessBO.getMajorEvaluationProcessId());
        if (majorEvaluationProcess == null) {
            throw new RuntimeException("专业评审流程不存在");
        }

        UpdateWrapper<MajorEvaluationProcess> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("process_end_status", END);
        updateWrapper.set("remark", endEvaluationProcessBO.getRemark());
        updateWrapper.eq("id", majorEvaluationProcess.getId());

        majorEvaluationProcess.update(updateWrapper);
        return true;
    }

    private void persistMasterEvaluation(MasterEvaluateBO masterEvaluateBO,
                                         MajorEvaluationProcess process,
                                         Integer userId,
                                         boolean requireExisting) {
        Long majorEvaluationProcessId = Long.valueOf(masterEvaluateBO.getMajorEvaluationProcessId());
        MasterEvaluation existingEvaluation = getMasterEvaluation(majorEvaluationProcessId, userId);

        if (requireExisting && existingEvaluation == null) {
            throw new CustomException("当前评审记录不存在，无法更新");
        }

        if (existingEvaluation != null) {
            clearMasterEvaluationRecords(existingEvaluation, majorEvaluationProcessId, userId);
        }

        for (Map.Entry<Integer, String> option : masterEvaluateBO.getOptionMap().entrySet()) {
            OptionRecord optionRecord = new OptionRecord(userId, majorEvaluationProcessId, option.getKey(), option.getValue());
            if (!optionRecord.insert()) {
                throw new CustomException("保存报告错误，请重新提交！或联系管理员！");
            }
        }

        UpdateWrapper<MajorEvaluationProcess> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("expert_review_status", END);
        updateWrapper.set("expert_leader_review_status", UNDERWAY);
        updateWrapper.eq("id", process.getId());
        process.update(updateWrapper);

        MasterEvaluation masterEvaluation = new MasterEvaluation(userId, majorEvaluationProcessId, masterEvaluateBO.getOpinion(), masterEvaluateBO.getRemark());
        masterEvaluation.setMajorId(process.getMajorId());
        if (!masterEvaluation.insert()) {
            throw new CustomException("评审表保存失败");
        }
    }

    private MasterEvaluation getMasterEvaluation(Long majorEvaluationProcessId, Integer userId) {
        QueryWrapper<MasterEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId);
        queryWrapper.eq("user_id", userId);
        return masterEvaluationMapper.selectOne(queryWrapper);
    }

    private void clearMasterEvaluationRecords(MasterEvaluation existingEvaluation,
                                              Long majorEvaluationProcessId,
                                              Integer userId) {
        QueryWrapper<OptionRecord> optionRecordQueryWrapper = new QueryWrapper<>();
        optionRecordQueryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId);
        optionRecordQueryWrapper.eq("user_id", userId);
        OptionRecord optionRecord = new OptionRecord();
        List<OptionRecord> optionRecords = optionRecord.selectList(optionRecordQueryWrapper);
        if (!optionRecords.isEmpty() && !optionRecord.delete(optionRecordQueryWrapper)) {
            throw new CustomException("修改问题列表失败！");
        }

        if (!existingEvaluation.deleteById()) {
            throw new CustomException("专家记录删除失败");
        }
    }

    private void validateOptionMap(Map<Integer, String> optionMap) {
        if (optionMap == null || optionMap.isEmpty()) {
            throw new CustomException("评审选项不能为空");
        }
    }
}
