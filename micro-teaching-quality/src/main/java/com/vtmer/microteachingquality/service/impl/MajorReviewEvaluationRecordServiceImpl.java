package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.domain.MajorReviewEvaluationRecord;
import com.vtmer.microteachingquality.mapper.MajorEvaluationProcessMapper;
import com.vtmer.microteachingquality.mapper.MajorReviewEvaluationRecordMapper;
import com.vtmer.microteachingquality.mapper.MajorReviewEvaluationUserMapper;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorReviewEvaluationUser;
import com.vtmer.microteachingquality.service.MajorReviewEvaluationRecordService;
import com.vtmer.microteachingquality.util.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author Cedirc_Adie
 * @description 针对表【major_review_evaluation_record】的数据库操作Service实现
 * @createDate 2023-12-13 20:18:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MajorReviewEvaluationRecordServiceImpl extends ServiceImpl<MajorReviewEvaluationRecordMapper, MajorReviewEvaluationRecord>
        implements MajorReviewEvaluationRecordService {

    @Resource
    MajorEvaluationProcessMapper majorEvaluationProcessMapper;
    @Resource
    MajorReviewEvaluationUserMapper majorReviewEvaluationUserMapper;

    private Boolean checkAble(Integer clazzId) {
        Integer id = UserUtil.getCurrentUser().getId();
        QueryWrapper<MajorReviewEvaluationUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id).eq("major_id", clazzId);
        MajorReviewEvaluationUser res = majorReviewEvaluationUserMapper.selectOne(queryWrapper);
        return res != null;
    }

    @Override
    public String insert(long evaluationId, int necessary) {
        MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectById(evaluationId);
        if (!checkAble(majorEvaluationProcess.getMajorId())) return null;
        if (necessary == 0) {
            majorEvaluationProcess.setAssessAgainStatus(0);
            majorEvaluationProcessMapper.updateById(majorEvaluationProcess);
            return "取消复评成功";
        }

        MajorReviewEvaluationRecord record = new MajorReviewEvaluationRecord();
        record.setEvaluationId(evaluationId);
        record.setEvaluationYear(majorEvaluationProcess.getEvaluationYear());
        baseMapper.insert(record);
        majorEvaluationProcess.setAssessAgainStatus(1);
        majorEvaluationProcessMapper.updateById(majorEvaluationProcess);
        return "创建复评流程成功";

    }

    @Override
    public MajorReviewEvaluationRecord getMajorReviewInfo(long evaluationId) {
        return baseMapper.selectOne(
                new QueryWrapper<MajorReviewEvaluationRecord>()
                        .eq("evaluation_id", evaluationId));
    }

    @Override
    public Boolean endMajorReviewOpinion(MajorReviewEvaluationRecord majorReviewEvaluationRecord) {
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(majorReviewEvaluationRecord.getEvaluationId());
        if (process == null) return null;
        if (checkAble(process.getMajorId())) return null;
        baseMapper.updateById(majorReviewEvaluationRecord);

        process.setAssessAgainStatus(2);
        int update = majorEvaluationProcessMapper.updateById(process);
        return update == 1;
    }
}




