package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus;
import com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.LeaderEvaluation;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MasterEvaluation;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.OptionRecord;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationResult;
import com.vtmer.microteachingquality.model.vo.MajorFinishQuestionResult;
import com.vtmer.microteachingquality.model.vo.OptionResult;
import com.vtmer.microteachingquality.service.MajorEvaluationRecodeService;
import com.vtmer.microteachingquality.service.ReportService;
import com.vtmer.microteachingquality.util.OptionRecordUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Colin_Knight
 * @create 2023/5/9 22:23
 */
@Service
public class MajorEvaluationRecodeServiceImpl implements MajorEvaluationRecodeService, EvaluationProcessStatus, UserTypeConstant {


    @Resource
    private OptionRecordMapper optionRecordMapper;

    @Resource
    private MajorEvaluationProcessMapper majorEvaluationProcessMapper;

    @Resource
    private MasterEvaluationMapper masterEvaluationMapper;

    @Resource
    private LeaderEvaluationMapper leaderEvaluationMapper;

    @Resource
    private MajorMapper majorMapper;


    @Resource
    private ReportService reportService;

    @Resource
    private OptionRecordUtil optionRecordUtil;


    public MajorEvaluationResult getSingleMajorFinishReview(Long majorEvaluationProcessId, Integer userId) {

        //获取意见信息
        QueryWrapper<MasterEvaluation> masterEvaluationQueryWrapper = new QueryWrapper<>();

        masterEvaluationQueryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId).eq("user_id", userId);

        MasterEvaluation masterEvaluation = masterEvaluationMapper.selectOne(masterEvaluationQueryWrapper);

        if (masterEvaluation == null) {
            throw new CustomException("未查到该专家评审");
        }

        //QW获取option_result的数据
        QueryWrapper<OptionRecord> optionResultQueryWrapper = new QueryWrapper<>();

        optionResultQueryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId).eq("user_id", userId);

        List<OptionRecord> optionRecords = optionRecordMapper.selectList(optionResultQueryWrapper);


        Major major = majorMapper.selectById(majorEvaluationProcessMapper.selectById(majorEvaluationProcessId).getMajorId());


        List<MajorFinishQuestionResult> majorFinishQuestionResults = optionRecords.stream().map(optionRecord -> {

            return new MajorFinishQuestionResult(optionRecord.getOptionId(), optionRecord.getMark());
        }).collect(Collectors.toList());

        //获取指标内容
        List<OptionResult> optionResults = reportService.listOptionByType(major.getName());

        return new MajorEvaluationResult(majorEvaluationProcessId, userId, majorFinishQuestionResults, masterEvaluation.getOpinion(), masterEvaluation.getRemark(), optionResults);


    }

    @Override
    public MajorEvaluationResult getLeaderMajorFinishReview(Long majorEvaluationProcessId, Integer userId) {
        //获取意见信息
        QueryWrapper<LeaderEvaluation> leaderEvaluationQueryWrapper = new QueryWrapper<>();

        leaderEvaluationQueryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId).eq("user_id", userId);

        LeaderEvaluation leaderEvaluation = leaderEvaluationMapper.selectOne(leaderEvaluationQueryWrapper);


        if (leaderEvaluation == null) {
            throw new CustomException("未查到该专家评审");
        }

        Major major = majorMapper.selectById(majorEvaluationProcessMapper.selectById(majorEvaluationProcessId).getMajorId());


        List<OptionResult> optionResults = reportService.listOptionByType(major.getName());


        //QW获取option_result的数据
        QueryWrapper<OptionRecord> optionResultQueryWrapper = new QueryWrapper<>();

        optionResultQueryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId).eq("user_id", userId);

        List<OptionRecord> optionRecords = optionRecordMapper.selectList(optionResultQueryWrapper);

        List<MajorFinishQuestionResult> majorFinishQuestionResults = optionRecords.stream().map(optionRecord -> {
            return new MajorFinishQuestionResult(optionRecord.getOptionId(), optionRecord.getMark());
        }).collect(Collectors.toList());


        return new MajorEvaluationResult(majorEvaluationProcessId, userId, majorFinishQuestionResults, leaderEvaluation.getOpinion(), leaderEvaluation.getResult(), optionResults);

    }


}
