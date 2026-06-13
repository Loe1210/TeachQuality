package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.mapper.ClazzEvaluationProcessMapper;
import com.vtmer.microteachingquality.mapper.ClazzOpinionRecordMapper;
import com.vtmer.microteachingquality.mapper.ClazzReviewEvaluationRecordMapper;
import com.vtmer.microteachingquality.mapper.ClazzReviewEvaluationUserMapper;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzOpinionRecord;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzReviewEvaluationRecord;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzReviewEvaluationUser;
import com.vtmer.microteachingquality.model.vo.ClazzReviewInfo;
import com.vtmer.microteachingquality.service.ClazzReviewEvaluationRecordService;
import com.vtmer.microteachingquality.util.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Cedirc_Adie
 * @description 针对表【clazz_review_evaluation_record】的数据库操作Service实现
 * @createDate 2023-12-10 22:36:30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ClazzReviewEvaluationRecordServiceImpl extends ServiceImpl<ClazzReviewEvaluationRecordMapper, ClazzReviewEvaluationRecord>
        implements ClazzReviewEvaluationRecordService {

    @Resource
    ClazzOpinionRecordMapper clazzOpinionRecordMapper;
    @Resource
    ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;
    @Resource
    ClazzReviewEvaluationUserMapper clazzReviewEvaluationUserMapper;

    private Boolean checkAble(Integer clazzId) {
        Integer id = UserUtil.getCurrentUser().getId();
        QueryWrapper<ClazzReviewEvaluationUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id).eq("clazz_id", clazzId);
        ClazzReviewEvaluationUser res = clazzReviewEvaluationUserMapper.selectOne(queryWrapper);
        return res != null;
    }

    @Override
    public String insert(long evaluationId, int necessary) {
        ClassEvaluationProcess classEvaluationProcess = clazzEvaluationProcessMapper.selectById(evaluationId);
        if (!checkAble(classEvaluationProcess.getClazzId())) return null;
        if (necessary == 0) {
            classEvaluationProcess.setAssessAgainStatus(0);
            clazzEvaluationProcessMapper.updateById(classEvaluationProcess);
            return "取消复评成功";
        }
        ClazzReviewEvaluationRecord clazzReviewEvaluationRecord = new ClazzReviewEvaluationRecord();
        clazzReviewEvaluationRecord.setEvaluationId(evaluationId);
        clazzReviewEvaluationRecord.setEvaluationYear(classEvaluationProcess.getEvaluationYear());
        baseMapper.insert(clazzReviewEvaluationRecord);
        classEvaluationProcess.setAssessAgainStatus(1);
        clazzEvaluationProcessMapper.updateById(classEvaluationProcess);
        return "创建复评流程成功";
    }

    @Override
    public ClazzReviewInfo getClazzReviewInfo(long evaluationId) {
        ClazzReviewEvaluationRecord clazzReviewEvaluationRecord = baseMapper.selectById(evaluationId);
        if (clazzReviewEvaluationRecord == null) {
            return null;
        }

        return new ClazzReviewInfo(clazzReviewEvaluationRecord.getId(),
                clazzReviewEvaluationRecord.getEvaluationId(),
                clazzReviewEvaluationRecord.getReviewEvaluation(),
                clazzReviewEvaluationRecord.getRemark(),
                clazzReviewEvaluationRecord.getEvaluationYear());
    }

    @Override
    public List<ClazzOpinionRecord> getClazzOpinions(long clazzId) {
        ClassEvaluationProcess exist = clazzEvaluationProcessMapper.selectEvaluationByClazzId(clazzId);
        if (exist == null) return null;

        QueryWrapper<ClazzOpinionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_id", exist.getClazzId());
        return clazzOpinionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public Boolean endClazzReviewOpinion(ClazzReviewEvaluationRecord clazzReviewEvaluationRecord) {
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(clazzReviewEvaluationRecord.getEvaluationId());
        if (process == null) return null;
        if (!checkAble(process.getClazzId())) return null;
        baseMapper.updateById(clazzReviewEvaluationRecord);
        process.setAssessAgainStatus(2);
        int update = clazzEvaluationProcessMapper.updateById(process);
        return update == 1;
    }


}




