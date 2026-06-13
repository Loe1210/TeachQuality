package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.vtmer.microteachingquality.common.component.EvaluationMessageProducer;
import com.vtmer.microteachingquality.common.constant.topic.ClazzEvaluationTopic;
import com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus;
import com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.bo.SubmitEvaluationRecordBO;
import com.vtmer.microteachingquality.model.dto.ClazzInJudgeResultDTO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.*;
import com.vtmer.microteachingquality.model.vo.*;
import com.vtmer.microteachingquality.service.CourseEvaluationExpertService;
import com.vtmer.microteachingquality.util.UserUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hung
 */
@Service
@Slf4j
public class CourseEvaluationExpertServiceImpl implements CourseEvaluationExpertService, EvaluationProcessStatus, UserTypeConstant {

    @Resource
    private ClazzExpertManageInfoMapper clazzExpertManageInfoMapper;
    @Resource
    private ClazzMapper clazzMapper;
    @Resource
    private ClazzFileMapper clazzFileMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ClazzEvaluateOptionMapper clazzEvaluateOptionMapper;
    @Resource
    private ClazzEvaluateOptionRecordMapper recordMapper;
    @Resource
    private ClazzOpinionRecordMapper clazzOpinionRecordMapper;
    @Resource
    private ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;
    @Resource
    private EvaluationMessageProducer evaluationMessageProducer;

    /**
     * 评审课程报告信息
     *
     * @return 返回评审的课程报告信息
     */
    @Override
    public FinishedClazzReviewVO getCourseMessageOfEvaluationExpert(Integer status, Integer pageNum, Integer pageSize) {
        Integer userId = UserUtil.getCurrentUser().getId();
        Integer startIndex = (pageNum - 1) * pageSize;
        Integer length = pageSize;
        List<ClazzExpertManageInfo> recordList = clazzExpertManageInfoMapper.selectByUserIdAndStatus(userId, status, startIndex, length);

        FinishedClazzReviewVO result = new FinishedClazzReviewVO();
        List<ClazzToReviewResult> resultList = new ArrayList<>();
        //先获取了所有管理的信息，再根据课程id和状态去进行查询
        for (ClazzExpertManageInfo expertManageInfo : recordList) {

            Clazz clazz = clazzMapper.selectByPrimaryKey(expertManageInfo.getClazzId());
            if (clazz == null) {
                continue;
            }
            ClazzToReviewResult resultTemp = new ClazzToReviewResult();
            resultTemp.setClazzName(clazz.getName());
            resultTemp.setClazzCollege(clazz.getCollege());
            resultTemp.setClazzId(clazz.getId());
            List<User> userList = userMapper.selectByUserBelong(clazz.getName());
            if (userList.size() == 1) {
                //通过userBelong获取的虽然是list，但应该只有一个用户，所以直接get下标0
                User user = userList.get(0);
                resultTemp.setPrincipalName(user.getRealName());
            }
            resultList.add(resultTemp);

        }
        result.setClazzToReviewResultList(resultList);
        result.setTotalCounts(clazzExpertManageInfoMapper.countClazzToReview(userId, status));

        return result;
    }

    /**
     * 获取评审信息
     *
     * @param clazzId 课程id
     * @return 返回评审信息
     */
    @Override
    public List<ClazzInJudgeResultDTO> getClazzInJudgeByClazzType(Integer clazzId) {
        //获取课程的类型
        String type = clazzMapper.selectById(clazzId).getType();
        //根据类型获取评审信息
        List<ClazzEvaluateOption> optionList = clazzEvaluateOptionMapper.selectByClazzType(type);
        return optionList.stream()
                .map(clazzEvaluateOption -> new ClazzInJudgeResultDTO(clazzEvaluateOption.getId(), clazzEvaluateOption.getFirstTarget(), clazzEvaluateOption.getDetail()))
                .collect(Collectors.toList());
    }

    @Override
    public ClazzFinishResult getSingleClazzFinishReview(Long evaluationId, Integer userId) {

        //用QW获取数据
        QueryWrapper<ClazzEvaluateOptionRecord> clazzEvaluateOptionRecordQueryWrapper = new QueryWrapper<>();
        clazzEvaluateOptionRecordQueryWrapper.eq("clazz_evaluation_process_id", evaluationId)
                .eq("user_id", userId);

        //获取选择信息
        List<ClazzEvaluateOptionRecord> optionRecordList = recordMapper.selectList(clazzEvaluateOptionRecordQueryWrapper);

        List<ClazzFinishQuestionResult> questionResultList = optionRecordList.stream().map(clazzEvaluateOptionRecord -> {
            ClazzEvaluateOption option = clazzEvaluateOptionMapper.selectByPrimaryKey(clazzEvaluateOptionRecord.getEvaluationOptionId());
            return new ClazzFinishQuestionResult(option.getId(), option.getFirstTarget(), option.getDetail(), clazzEvaluateOptionRecord.getMark());
        }).collect(Collectors.toList());

        //获取意见信息
        QueryWrapper<ClazzOpinionRecord> clazzOpinionRecordQueryWrapper = new QueryWrapper<>();
        clazzOpinionRecordQueryWrapper.eq("clazz_evaluation_process_id", evaluationId).eq("user_id", userId);
        ClazzOpinionRecord clazzOpinionRecord = clazzOpinionRecordMapper.selectOne(clazzOpinionRecordQueryWrapper);

        return new ClazzFinishResult(evaluationId, userId, questionResultList, clazzOpinionRecord.getClazzAdvantage(), clazzOpinionRecord.getClazzProblem(), clazzOpinionRecord.getClazzAdvice(), clazzOpinionRecord.getClazzRemark());
    }

    /**
     * @author 达
     * 课程评审专家提交评审记录
     */
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean insertEvaluationRecord(SubmitEvaluationRecordBO submitEvaluationRecordBO) {
        //TODO 修改评审得重新设计这里，因为前端是不知道用户id，所以不知道是谁在提交评审。

        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(submitEvaluationRecordBO.getClazzEvaluationProcessId());
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        //其实也可以这么理解，如果一开始没有记录,第一个插入肯定是负责人，然后后面修改的可能是负责人 也可能是专家组长

        //先查是否有记录
        //删除 这个人的选择和评审记录
        QueryWrapper<ClazzEvaluateOptionRecord> optionRecordQueryWrapper = new QueryWrapper<>();
        optionRecordQueryWrapper.eq("clazz_evaluation_process_id", submitEvaluationRecordBO.getClazzEvaluationProcessId());
        optionRecordQueryWrapper.eq("user_id", submitEvaluationRecordBO.getOriginReviewExpertId());
        List<ClazzEvaluateOptionRecord> clazzEvaluateOptionRecords = clazzEvaluateOptionMapper.selectList(optionRecordQueryWrapper);
        if (clazzEvaluateOptionRecords != null && !clazzEvaluateOptionRecords.isEmpty()) {
            //有记录，就代表不是第一次评审，删除原来的记录
            clazzEvaluateOptionRecords.forEach(Model::deleteById);
        }

        QueryWrapper<ClazzOpinionRecord> recordQueryWrapper = new QueryWrapper<>();
        recordQueryWrapper.eq("clazz_evaluation_process_id", submitEvaluationRecordBO.getClazzEvaluationProcessId());
        recordQueryWrapper.eq("user_id", submitEvaluationRecordBO.getOriginReviewExpertId());
        ClazzOpinionRecord clazzOpinionRecord = clazzOpinionRecordMapper.selectOne(recordQueryWrapper);
        if (clazzOpinionRecord != null) {
            clazzOpinionRecord.deleteById();
        }

        submitEvaluationRecordBO.setOriginReviewExpertId(UserUtil.getCurrentUser().getId());

        //将选择题 评审插入
        submitEvaluationRecordBO.getInsertEvaluationRecordBOList()
                .forEach(insertEvaluationRecordDTO -> new ClazzEvaluateOptionRecord(submitEvaluationRecordBO.getOriginReviewExpertId(),
                        submitEvaluationRecordBO.getClazzEvaluationProcessId(), insertEvaluationRecordDTO.getOptionId(),
                        insertEvaluationRecordDTO.getMark()).insert());

        //将填空题 插入
        ClazzOpinionRecord opinionRecord = new ClazzOpinionRecord(submitEvaluationRecordBO.getOriginReviewExpertId(), submitEvaluationRecordBO.getAdvantage(), submitEvaluationRecordBO.getProblem(), submitEvaluationRecordBO.getAdvice(), submitEvaluationRecordBO.getRemark(), submitEvaluationRecordBO.getClazzEvaluationProcessId());
        clazzOpinionRecordMapper.insert(opinionRecord);

        User user = UserUtil.getCurrentUser();

        log.info("用户id {} {} 的userRole是：{}", user.getId(), user.getRealName(), user.getRoles().toString());

        //创建QW来更新修改状态
        QueryWrapper<ClassEvaluationProcess> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("evaluation_id", submitEvaluationRecordBO.getClazzEvaluationProcessId());


        //更新当前评审流程状态
        if ((UserUtil.isRoleAvailable(CLAZZ_EVALUATION_EXPERT_LEADER) || UserUtil.isRoleAvailable(TEST_CLAZZ_EVALUATION_EXPERT_LEADER))
                && process.getExpertReviewStatus().equals(END)) {
//            clazzEvaluationProcessMapper.updateEvaluationProcessOnExpertLeaderReview(submitEvaluationRecordBO.getClazzEvaluationProcessId());

            ClassEvaluationProcess classEvaluationProcess = new ClassEvaluationProcess();
            classEvaluationProcess.setExpertLeaderReviewStatus(2);
            classEvaluationProcess.setExpertGroupReviewStatus(1);

            int update = clazzEvaluationProcessMapper.update(classEvaluationProcess, queryWrapper);
            log.info("用户id {} {} 修改表数据成功，返回数据：{}行，修改的evaluation_process_id为：{}", user.getId(), user.getRealName(), update, classEvaluationProcess.getId());
            queryWrapper.clear();

        }
        if (UserUtil.isRoleAvailable(CLAZZ_EVALUATION_EXPERT) || UserUtil.isRoleAvailable(TEST_CLAZZ_EVALUATION_EXPERT)) {

            ClassEvaluationProcess classEvaluationProcess = new ClassEvaluationProcess();
            classEvaluationProcess.setExpertReviewStatus(2);
            classEvaluationProcess.setExpertLeaderReviewStatus(1);

            int update = clazzEvaluationProcessMapper.update(classEvaluationProcess, queryWrapper);
            log.info("用户id {} {} 修改表数据成功，返回数据：{}行，修改的evaluation_process_id为：{}", user.getId(), user.getRealName(), update, classEvaluationProcess.getId());
            queryWrapper.clear();

        }

        if (UserUtil.isRoleAvailable(CLAZZ_EVALUATION_EXPERT_LEADER)) {
            evaluationMessageProducer.sendClazzEvaluationMessage(
                    ClazzEvaluationTopic.EXPERT_LEADER_SUBMIT,
                    UserUtil.getCurrentUser().getId(),
                    submitEvaluationRecordBO.getClazzEvaluationProcessId());
            return true;
        }
        if (UserUtil.isRoleAvailable(CLAZZ_EVALUATION_EXPERT)) {
            evaluationMessageProducer.sendClazzEvaluationMessage(
                    ClazzEvaluationTopic.EXPERT_SUBMIT,
                    UserUtil.getCurrentUser().getId(),
                    submitEvaluationRecordBO.getClazzEvaluationProcessId());
        }
        return true;
    }


    /**
     * 根据课程名字返回文件列表
     */
    @Override
    public List<GetClazzFilesResult> getAllEvaluationFiles(Long evaluationId) {
        QueryWrapper<ClazzFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_evaluation_process_id", evaluationId);
        return clazzFileMapper.selectList(queryWrapper)
                .stream()
                .map(clazzFile -> new GetClazzFilesResult(clazzFile.getId(), clazzFile.getUserId(), clazzFile.getClazzId(), clazzFile.getFileName(), clazzFile.getPath()))
                .collect(Collectors.toList());
    }


    @Override
    public Integer insertEmptyData(String tableName, Integer size) {
        int result = 0;
        for (int i = 0; i < size; i++) {
            result += clazzMapper.insertEmptyData(tableName);
        }
        return result;
    }

}
