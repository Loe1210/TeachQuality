package com.vtmer.microteachingquality.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.common.component.EvaluationMessageProducer;
import com.vtmer.microteachingquality.common.constant.topic.ClazzEvaluationTopic;
import com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus;
import com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.bo.ClazzEvaluationLeaderBO;
import com.vtmer.microteachingquality.model.dto.ClazzOpinionRecordDTO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.*;
import com.vtmer.microteachingquality.model.vo.*;
import com.vtmer.microteachingquality.service.ClazzEvaluationProcessService;
import com.vtmer.microteachingquality.service.ClazzFileService;
import com.vtmer.microteachingquality.service.ClazzService;
import com.vtmer.microteachingquality.util.UserUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

/**
 * @author Hung
 * @date 2022/4/20 20:02
 */
@Service
@Slf4j
public class ClazzEvaluationProcessServiceImpl extends ServiceImpl<ClazzEvaluationProcessMapper, ClassEvaluationProcess> implements ClazzEvaluationProcessService, EvaluationProcessStatus, UserTypeConstant {

    /**
     * 随机生成存储加密文件名(课程自评报告)的密钥
     * 固定密钥，不然会出bug
     */
    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();


    /**
     * 构建加密
     */
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);
    @Value("${clazz.path}")
    private String clazzPath;
    @Resource
    private ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;
    @Resource
    private ClazzFileMapper clazzFileMapper;
    @Resource
    private ClazzOpinionRecordMapper clazzOpinionRecordMapper;
    @Resource
    private ClazzOpinionLeaderRecordMapper clazzOpinionLeaderRecordMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ClazzMapper clazzMapper;
    @Resource
    private ClazzService clazzService;
    @Resource
    private ClazzFileService clazzFileService;
    @Resource
    private EvaluationMessageProducer evaluationMessageProducer;


    @SneakyThrows
    @Override
    public Boolean createEvaluationProcess(Integer clazzId) {
        //判断课程是否存在
        Clazz clazz = clazzMapper.selectByPrimaryKey(clazzId);
        if (clazz == null) {
            throw new CustomException("课程不存在");
        }

        User user = UserUtil.getCurrentUser();
        //提前生成id，保持一致性
        Long processId = IdUtil.getSnowflake(0, 0).nextId();
        ClassEvaluationProcess process = new ClassEvaluationProcess(processId, user.getId(), clazzId, LocalDateTime.now().getYear() + "级");
        if (process.insert()) {
            evaluationMessageProducer.sendClazzEvaluationMessage(ClazzEvaluationTopic.PROCESS_CREATED, user.getId(), processId);
            log.info("用户id {} {} 创建课程评价流程成功，流程id：{}", user.getId(), user.getRealName(), processId);
            return true;
        }
        log.info("用户id {} {} 创建课程评价流程失败", user.getId(), user.getRealName());
        return false;
    }

    /**
     * 删除 评审流程
     *
     * @param clazzEvaluationProcessId 流程id
     * @return 是否删除成功
     */
    @Override
    public String deleteEvaluationProcess(Long clazzEvaluationProcessId) {
        User loginUser = UserUtil.getCurrentUser();
        ClassEvaluationProcess classEvaluationProcess = clazzEvaluationProcessMapper.selectById(clazzEvaluationProcessId);

        if (classEvaluationProcess.getCreatorId().equals(loginUser.getId()) || UserUtil.isRoleAvailable(MASTER)) {
            clazzEvaluationProcessMapper.deleteById(clazzEvaluationProcessId);
            return "删除成功";
        }

        return "只有管理员和创建者有删除权限";
    }

    @Override
    public List<ClazzEvaluationProcessSimpleInfoVO> getEvaluationProcesses(Integer clazzId, Integer pageSize, Integer pageNum, Map<String, String> conditionMap) {
        //判断课程是否存在
        Clazz clazz = clazzMapper.selectByPrimaryKey(clazzId);
        if (clazz == null) {
            throw new CustomException("课程不存在");
        }

        QueryWrapper<ClassEvaluationProcess> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_id", clazzId);
        log.info(queryWrapper.getSqlSelect() + queryWrapper.getSqlSegment());
        List<ClassEvaluationProcess> classEvaluationProcessList = clazzEvaluationProcessMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper).getRecords();
        log.info("查询到的课程评价流程列表：{}", classEvaluationProcessList);
        if (classEvaluationProcessList.isEmpty()) {
            return new ArrayList<>();
        }

        return classEvaluationProcessList.stream().map(classEvaluationProcess -> {
            ClazzEvaluationProcessSimpleInfoVO clazzEvaluationProcessSimpleInfoVO = new ClazzEvaluationProcessSimpleInfoVO();
            BeanUtils.copyProperties(classEvaluationProcess, clazzEvaluationProcessSimpleInfoVO);

            clazzEvaluationProcessSimpleInfoVO.setId(Long.toString(classEvaluationProcess.getId()));
            clazzEvaluationProcessSimpleInfoVO.setCreatorName(userMapper.selectById(classEvaluationProcess.getCreatorId()).getRealName());

            if (classEvaluationProcess.getProcessEndStatus() == 2) {
                clazzEvaluationProcessSimpleInfoVO.setStatus("流程结束");
                return clazzEvaluationProcessSimpleInfoVO;
            }

            if (classEvaluationProcess.getExpertGroupReviewStatus() == 2) {
                clazzEvaluationProcessSimpleInfoVO.setStatus("专家小组评审完成");
                return clazzEvaluationProcessSimpleInfoVO;
            }

            if (classEvaluationProcess.getExpertLeaderReviewStatus() == 2) {
                clazzEvaluationProcessSimpleInfoVO.setStatus("专家组长评审完成");
                return clazzEvaluationProcessSimpleInfoVO;
            }

            if (classEvaluationProcess.getExpertReviewStatus() == 2) {
                clazzEvaluationProcessSimpleInfoVO.setStatus("专家评审完成");
                return clazzEvaluationProcessSimpleInfoVO;
            }

            if (classEvaluationProcess.getPrincipalMaterialStatus() == 2) {
                clazzEvaluationProcessSimpleInfoVO.setStatus("负责人完成上传材料");
                return clazzEvaluationProcessSimpleInfoVO;
            }

            if (classEvaluationProcess.getPrincipalMaterialStatus() == 1) {
                clazzEvaluationProcessSimpleInfoVO.setStatus("等待负责人上传材料");
                return clazzEvaluationProcessSimpleInfoVO;
            }

            return clazzEvaluationProcessSimpleInfoVO;
        }).collect(Collectors.toList());

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean principalUploadMaterial(MultipartFile file, Long evaluationId) {
        // 获取当前登陆用户(课程负责人)对象
        User user = UserUtil.getCurrentUser();

        if (StrUtil.isBlank(file.getOriginalFilename())) {
            throw new CustomException("请选择需要上传的文件");
        }

        // 目录添加uuid防重
        String uuid = IdUtil.simpleUUID();
        File path = new File(clazzPath + File.separator + uuid);
        if (!path.isDirectory()) {
            path.mkdirs();
        }

        //生成文件路径
        String filePath = clazzPath + File.separator + uuid + File.separator + file.getOriginalFilename();

        // 加密文件名
        String encryptFileName = aes.encryptHex(uuid + File.separator + file.getOriginalFilename());

        //该评审流程的负责人登录，查询是否已经有此报告了
        ClazzFile clazzFile = clazzService.exitFile(user.getId(), evaluationId, file.getOriginalFilename());

        if (clazzFile != null) {
            //数据库中已经有记录,报错
            clazzService.deleteUploadedFile(clazzFile.getPath());

//            throw new CustomException(EvaluationProcessStatus.FILE_EXIST);
        }

        //clazzFile == null说明数据库中没有还没有这个负责人上传该课程评审的相同报告
        Integer clazzId = clazzEvaluationProcessMapper.selectById(evaluationId).getClazzId();

        // 存储课程报告上传信息(修改课程报告上传者、文件路径)
        ClazzFile clazzFileInsert = new ClazzFile();
        clazzFileInsert.setUserId(user.getId());
        clazzFileInsert.setFileName(file.getOriginalFilename());
        clazzFileInsert.setClazzId(clazzId);
        clazzFileInsert.setClazzEvaluationProcessId(evaluationId);
        clazzFileInsert.setPath(encryptFileName);

        //更改课程评审流程状态
        ClassEvaluationProcess classEvaluationProcess = clazzEvaluationProcessMapper.selectById(evaluationId);
        //这里判断防止在评审过程中 负责人更改材料造成的状态混乱
        if (classEvaluationProcess.getPrincipalMaterialStatus().equals(UNDERWAY)) {
            classEvaluationProcess.setPrincipalMaterialStatus(END);
            classEvaluationProcess.setExpertReviewStatus(UNDERWAY);
            classEvaluationProcess.updateById();
        }

        //TODO 上传文件不会插入id
        if (clazzFileMapper.insert(clazzFileInsert) > 0) {
            evaluationMessageProducer.sendClazzEvaluationMessage(ClazzEvaluationTopic.PRINCIPAL_UPLOAD, user.getId(), evaluationId);
            FileUtil.writeBytes(file.getBytes(), filePath);
        }

        log.info("用户id {} {} 调用，流程id：{}", user.getId(), user.getRealName(), evaluationId);
        log.info("用户 {} {} 上传课程自评报告成功，流程id：{},文件名为 {} ", user.getId(), user.getRealName(), evaluationId, file.getOriginalFilename());
        return true;
    }

    @Override
    public List<GetUploadedFilesResult> getUploadedFiles(Integer userId, Long evaluationId) {
        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(evaluationId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        QueryWrapper<ClazzFile> clazzFileQueryWrapper = new QueryWrapper<>();
        clazzFileQueryWrapper.eq("user_id", userId);
        clazzFileQueryWrapper.eq("clazz_evaluation_process_id", evaluationId);
        List<ClazzFile> fileList = clazzFileMapper.selectList(clazzFileQueryWrapper);
        return fileList.stream()
                .map(clazzFile -> new GetUploadedFilesResult(clazzFile.getId(), clazzFile.getFileName(), clazzFile.getPath(), clazzFile.getUpdateTime()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendBackEvaluation(Long evaluationId) {

        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(evaluationId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        //更新评审流程状态
        UpdateWrapper<ClassEvaluationProcess> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("principal_material_status", MATERIAL_BACK);
        updateWrapper.set("expert_review_status", UNDERWAY);
        process.update(updateWrapper);

        //向被退回的课程负责人发送通知
        User currentUser = UserUtil.getCurrentUser();
        evaluationMessageProducer.sendClazzEvaluationMessage(ClazzEvaluationTopic.MATERIAL_BACK, currentUser.getId(), evaluationId);
        return true;
    }

    @Override
    public List<FinishedReviewVO> getAllFinishedReviews(Long evaluationProcessId) {
        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(evaluationProcessId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }


        List<ClazzOpinionRecordDTO> reviewInfo = clazzOpinionRecordMapper.getAllReviewInfo(evaluationProcessId);
        return reviewInfo.stream().map(clazzOpinionRecordDTO -> {
            User user = new User().selectById(clazzOpinionRecordDTO.getUserId());
            return new FinishedReviewVO(user.getId(), user.getRealName(), clazzOpinionRecordDTO.getUpdateTime());
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postClazzEvaluationLeaderReview(ClazzEvaluationLeaderBO evaluationLeaderBO) {

        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(evaluationLeaderBO.getClazzEvaluationProcessId());
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        //判断课程是否存在
        Clazz clazz = clazzMapper.selectByPrimaryKey(evaluationLeaderBO.getClazzId());
        if (clazz == null) {
            throw new CustomException("课程不存在");
        }

        ClazzOpinionLeaderRecord leaderRecord = new ClazzOpinionLeaderRecord();
        BeanUtils.copyProperties(evaluationLeaderBO, leaderRecord);
        leaderRecord.setUserId(UserUtil.getCurrentUser().getId());

        //更改课程评审流程的状态
        clazzEvaluationProcessMapper.updateEvaluationProcessOnPostLeaderGroupReview(evaluationLeaderBO.getClazzEvaluationProcessId());

        return leaderRecord.insert();
    }

    @Override
    public List<ClazzEvaluationLeaderReviewVO> getClazzEvaluationLeaderReviews(Long evaluationProcessId) {

        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(evaluationProcessId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        QueryWrapper<ClazzOpinionLeaderRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_evaluation_process_id", evaluationProcessId);
        List<ClazzOpinionLeaderRecord> records = clazzOpinionLeaderRecordMapper.selectList(queryWrapper);
        return records.stream().map(clazzOpinionLeaderRecord -> {
            User user = userMapper.selectByPrimaryKey(clazzOpinionLeaderRecord.getUserId());
            return new ClazzEvaluationLeaderReviewVO(user.getId(), user.getRealName(), clazzOpinionLeaderRecord.getEvaluationOpinion(), clazzOpinionLeaderRecord.getRemark(), clazzOpinionLeaderRecord.getUpdateTime());
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean endClazzEvaluationProcess(Long evaluationProcessId, String remark) {
        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(evaluationProcessId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        clazzEvaluationProcessMapper.updateEvaluationProcessOnEndProcess(evaluationProcessId, remark);
        return true;
    }

    @Override
    public Boolean sendBackExpertReview(Long evaluationProcessId, Integer userId) {
        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(evaluationProcessId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        return clazzEvaluationProcessMapper.updateClazzOpinionRecordStatus(evaluationProcessId, userId) > 0;
    }

    @Override
    public ClazzEvaluationProcessInfo getEvaluationProcessInfo(Long evaluationProcessId) {
        //获取评审流程基本信息
        ClassEvaluationProcess classEvaluationProcess = clazzEvaluationProcessMapper.selectById(evaluationProcessId);
        if (classEvaluationProcess == null) {
            throw new CustomException("评审流程不存在");
        }

        ClazzEvaluationProcessInfo clazzEvaluationProcessInfo = new ClazzEvaluationProcessInfo();
        BeanUtils.copyProperties(classEvaluationProcess, clazzEvaluationProcessInfo);
        User user = userMapper.selectById(classEvaluationProcess.getCreatorId());
        clazzEvaluationProcessInfo.setCreatorName(user.getRealName());

        //根据当前角色来决定是否有相应权限
        User currentUser = UserUtil.getCurrentUser();

        //判断是不是评审流程创建者
        if (classEvaluationProcess.getCreatorId().equals(currentUser.getId())) {
            clazzEvaluationProcessInfo.setFileReviseAuthority(1);
            return clazzEvaluationProcessInfo;
        }

        //判断此用户是否已经参与评审了
        QueryWrapper<ClazzOpinionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_evaluation_process_id", evaluationProcessId);
        queryWrapper.eq("user_id", currentUser.getId());

        ClazzOpinionRecord clazzOpinionRecords = clazzOpinionRecordMapper.selectOne(queryWrapper);
        if (clazzOpinionRecords != null) {
            clazzEvaluationProcessInfo.setIsReview(true);
        }

        //判断是评审专家还是评审专家组长
        if (UserUtil.isRoleAvailable(CLAZZ_EVALUATION_EXPERT) || UserUtil.isRoleAvailable(TEST_CLAZZ_EVALUATION_EXPERT)) {
            clazzEvaluationProcessInfo.setReviewAuthority(1);
            clazzEvaluationProcessInfo.setReviewInformationAuthority(1);
            return clazzEvaluationProcessInfo;
        }

        if (UserUtil.isRoleAvailable(CLAZZ_EVALUATION_EXPERT_LEADER) || UserUtil.isRoleAvailable(TEST_CLAZZ_EVALUATION_EXPERT_LEADER)) {
            clazzEvaluationProcessInfo.setReviewLeaderAuthority(1);
            clazzEvaluationProcessInfo.setReviewInformationAuthority(2);

            //是评审专家组长，判断是否参与了小组评审
            QueryWrapper<ClazzOpinionLeaderRecord> leaderRecordQueryWrapper = new QueryWrapper<>();
            leaderRecordQueryWrapper.eq("clazz_evaluation_process_id", evaluationProcessId);
            leaderRecordQueryWrapper.eq("user_id", currentUser.getId());
            List<ClazzOpinionLeaderRecord> leaderRecords = clazzOpinionLeaderRecordMapper.selectList(leaderRecordQueryWrapper);
            if (!leaderRecords.isEmpty()) {
                clazzEvaluationProcessInfo.setIsGroupReview(true);
            }

            return clazzEvaluationProcessInfo;
        }

        return clazzEvaluationProcessInfo;
    }

    /**
     * 结束专家小组评审
     *
     * @param evaluationProcessId 课程评审流程id
     * @return 是否结束成功
     */
    @Override
    public Boolean endGroupEvaluation(Long evaluationProcessId) {
        ClassEvaluationProcess classEvaluationProcess = clazzEvaluationProcessMapper.selectById(evaluationProcessId);
        if (classEvaluationProcess == null) {
            throw new CustomException("评审流程不存在");
        }

        classEvaluationProcess.setExpertGroupReviewStatus(2);
        return updateById(classEvaluationProcess);
    }
}
