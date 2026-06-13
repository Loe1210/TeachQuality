package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.bo.SelectClazzEvaluationProcessListBO;
import com.vtmer.microteachingquality.model.bo.SelectMajorEvaluationProcessListBO;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationProcess;
import com.vtmer.microteachingquality.model.vo.ClazzEvaluationProcessVO;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationProcessVO;
import com.vtmer.microteachingquality.service.EvaluationProcessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class EvaluationProcessServiceImpl implements EvaluationProcessService {

    @Resource
    ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;
    @Resource
    ClazzReviewEvaluationRecordMapper clazzReviewEvaluationRecordMapper;
    @Resource
    MajorEvaluationProcessMapper majorEvaluationProcessMapper;
    @Resource
    MajorReviewEvaluationRecordMapper majorReviewEvaluationRecordMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    ClazzMapper clazzMapper;
    @Resource
    MajorMapper majorMapper;

    /**
     * 根据条件查找流程
     *
     * @param selectClazzEvaluationProcessListBO 条件
     * @return 此课程的所有评审流程
     */
    @Override
    public List<ClazzEvaluationProcessVO> getClazzEvaluationByName(SelectClazzEvaluationProcessListBO selectClazzEvaluationProcessListBO) {
        if (selectClazzEvaluationProcessListBO.getFilter() == null) return null;
        String name = selectClazzEvaluationProcessListBO.getFilter().getKeywords();
        List<Integer> clazzIds = clazzMapper.getClazzIds(name);
        QueryWrapper<ClassEvaluationProcess> queryWrapper = new QueryWrapper<>();
        if (clazzIds.isEmpty()) queryWrapper.in("clazz_id", clazzIds);

        return clazzEvaluationProcessMapper.selectPage(new PageDTO<>(selectClazzEvaluationProcessListBO.getPageNum(), selectClazzEvaluationProcessListBO.getPageSize()), queryWrapper)
                .getRecords().stream().map(process -> {
                    ClazzEvaluationProcessVO processVO = new ClazzEvaluationProcessVO();
                    BeanUtils.copyProperties(process, processVO);
                    processVO.setClazzName(clazzMapper.getClazzName(process.getClazzId()));
                    processVO.setCreator(userMapper.selectUserNameById(process.getCreatorId()));
                    return processVO;
                }).collect(Collectors.toList());
    }

    /**
     * 根据专业名称查找流程
     *
     * @param selectMajorEvaluationProcessListBO 专业名称
     * @return 此专业的所有评审流程
     */
    @Override
    public List<MajorEvaluationProcessVO> getMajorEvaluationByName(SelectMajorEvaluationProcessListBO selectMajorEvaluationProcessListBO) {
        if (selectMajorEvaluationProcessListBO.getFilter() == null) return null;
        String name = selectMajorEvaluationProcessListBO.getFilter().getKeywords();
        List<Integer> majorIds = majorMapper.getMajorIds(name);
        QueryWrapper<MajorEvaluationProcess> queryWrapper = new QueryWrapper<>();
        if (!majorIds.isEmpty()) queryWrapper.in("major_id", majorIds);

        return majorEvaluationProcessMapper.selectPage(new PageDTO<>(selectMajorEvaluationProcessListBO.getPageNum(), selectMajorEvaluationProcessListBO.getPageSize()), queryWrapper)
                .getRecords().stream().map(process -> {
                    MajorEvaluationProcessVO processVO = new MajorEvaluationProcessVO();
                    BeanUtils.copyProperties(process, processVO);
                    processVO.setMajorName(majorMapper.getMajorName(process.getMajorId()));
                    processVO.setCreator(userMapper.selectUserNameById(process.getCreatorId()));
                    return processVO;
                }).collect(Collectors.toList());
    }

    /**
     * 删除对应流程
     *
     * @param t 流程所属类型
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteProcess(T t) throws NoSuchFieldException, IllegalAccessException {
        int delete;
        Field id = t.getClass().getDeclaredField("id");
        id.setAccessible(true);
        Object obj = id.get(t);
        if (t.getClass().toString().contains("major")) {
            // 专业评审流程
            delete = majorEvaluationProcessMapper.deleteById(obj.toString());
            clazzReviewEvaluationRecordMapper.deleteByEvaluationId(obj.toString());
        } else {
            // 课程评审流程
            delete = clazzEvaluationProcessMapper.deleteById(obj.toString());
            majorReviewEvaluationRecordMapper.deleteByEvaluationId(obj.toString());
        }

        return delete == 1;
    }

    /**
     * 修改专业评审流程的评审状态
     *
     * @param majorEvaluationProcess 修改后的评审流程
     * @return 是否修改成功
     */
    @Override
    public Boolean changeMajorProcessStatus(MajorEvaluationProcess majorEvaluationProcess) {
        MajorEvaluationProcess process = majorEvaluationProcessMapper.selectById(majorEvaluationProcess.getId());
        if (process == null) throw new CustomException("没有此评审流程");

        int update = majorEvaluationProcessMapper.updateById(majorEvaluationProcess);
        return update == 1;
    }

    /**
     * 修改课程评审流程的评审状态
     *
     * @param classEvaluationProcess 修改后的评审流程
     * @return 是否修改成功
     */
    @Override
    public Boolean changeClazzProcessStatus(ClassEvaluationProcess classEvaluationProcess) {
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(classEvaluationProcess.getId());
        if (process == null) throw new CustomException("没有此评审流程");

        int update = clazzEvaluationProcessMapper.updateById(classEvaluationProcess);
        return update == 1;
    }
}
