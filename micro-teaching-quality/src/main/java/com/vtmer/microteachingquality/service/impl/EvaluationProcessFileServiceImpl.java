package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.bo.ClazzEvaluationToWordBO;
import com.vtmer.microteachingquality.model.bo.MajorEvaluationToWordBO;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.Clazz;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzOpinionLeaderRecord;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.LeaderEvaluation;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import com.vtmer.microteachingquality.service.EvaluationProcessFileService;
import com.vtmer.microteachingquality.util.ExportEvaluationWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class EvaluationProcessFileServiceImpl implements EvaluationProcessFileService {

    //    @Resource
//    ClazzOpinionRecordMapper clazzOpinionRecordMapper;
    @Resource
    ClazzOpinionLeaderRecordMapper clazzOpinionLeaderRecordMapper;
    @Resource
    ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;
    @Resource
    LeaderEvaluationMapper leaderEvaluationMapper;
    @Resource
    MajorEvaluationProcessMapper majorEvaluationProcessMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    LeaderInfoMapper leaderInfoMapper;
    @Resource
    MajorMapper majorMapper;
    @Resource
    ClazzMapper clazzMapper;

    /**
     * 导出专业评审评估报告
     *
     * @param majorEvaluationProcessId 专业评审流程id
     * @return 储存路径
     */
    @Override
    public List<String> generateMajorEvaluationReport(Long majorEvaluationProcessId) {
        List<String> paths = new ArrayList<>();
        QueryWrapper<LeaderEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId);
        List<LeaderEvaluation> evaluations = leaderEvaluationMapper.selectList(queryWrapper);
        Major major = majorMapper.selectById(majorEvaluationProcessMapper.selectById(majorEvaluationProcessId).getMajorId());
        for (LeaderEvaluation evaluation : evaluations) {
            StringBuilder sb = new StringBuilder();
            leaderInfoMapper.selectMembersIdByLeaderId(evaluation.getUserId()).forEach(sb::append);
            MajorEvaluationToWordBO majorEvaluationToWordBO = new MajorEvaluationToWordBO();
            majorEvaluationToWordBO
                    .setCollegeAndMajor(major.getCollege() + major.getName())
                    .setDate(evaluation.getUpdateTime().toString().split("T")[0])
                    .setLeader(userMapper.selectUserNameById(evaluation.getUserId()))
                    .setMembers(sb.toString())
                    .setOpinion(evaluation.getOpinion())
                    .setResult(evaluation.getResult());
            String s = ExportEvaluationWordUtil.exportMajorEvaluationWord(majorEvaluationToWordBO);
            if (s != null) paths.add(s);
        }
        return paths;
    }

    /**
     * 导出课程评审评估报告
     *
     * @param clazzEvaluationProcessId 课程评审流程id
     * @return 储存路径
     */
    @Override
    public List<String> generateClazzEvaluationReport(Long clazzEvaluationProcessId) {
        List<String> paths = new ArrayList<>();
        QueryWrapper<ClazzOpinionLeaderRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_evaluation_process_id", clazzEvaluationProcessId);
        List<ClazzOpinionLeaderRecord> records = clazzOpinionLeaderRecordMapper.selectList(queryWrapper);
        try {
            Clazz clazz = clazzMapper.selectById(clazzEvaluationProcessMapper.selectById(clazzEvaluationProcessId).getClazzId());
            for (ClazzOpinionLeaderRecord record : records) {
                String opinion = record.getEvaluationOpinion();
                StringBuilder sb = new StringBuilder();
                List<String> list = leaderInfoMapper.selectMembersIdByLeaderId(record.getUserId());
                list.forEach(m -> sb.append(userMapper.selectUserNameById(Integer.valueOf(m))).append(" "));
                ClazzEvaluationToWordBO clazzEvaluationToWordBO = new ClazzEvaluationToWordBO();
                clazzEvaluationToWordBO
                        .setResult(noNullResult(record.getRemark()))
                        .setAdvantage(opinion)
                        .setAdvice(opinion)
                        .setLeader(noNullResult(userMapper.selectUserNameById(record.getUserId())))
                        .setName(noNullResult(clazz.getName()))
                        .setProblem(opinion)
                        .setDate(noNullResult(record.getUpdateTime().toString().split("T")[0]))
                        .setMembers(noNullResult(sb.toString()));
                String s = ExportEvaluationWordUtil.exportClazzEvaluationWord(clazzEvaluationToWordBO);
                if (s != null) paths.add(s);
            }
        } catch (Exception e) {
            System.out.println(clazzEvaluationProcessId);
        }

        return paths;
    }

    private String noNullResult(String origin) {
        return (origin == null || origin.equals("null") || origin.length() == 0) ? "无" : origin;
    }

}
