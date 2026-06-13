package com.vtmer.microteachingquality.service;

import com.vtmer.microteachingquality.model.bo.SelectClazzEvaluationProcessListBO;
import com.vtmer.microteachingquality.model.bo.SelectMajorEvaluationProcessListBO;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationProcess;
import com.vtmer.microteachingquality.model.vo.ClazzEvaluationProcessVO;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationProcessVO;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * @author batstroke
 */
public interface EvaluationProcessService {

    /**
     * 根据条件查找流程
     *
     * @param selectClazzEvaluationProcessListBO 条件
     * @return 此课程的所有评审流程
     */
    List<ClazzEvaluationProcessVO> getClazzEvaluationByName(SelectClazzEvaluationProcessListBO selectClazzEvaluationProcessListBO);

    /**
     * 根据专业名称查找流程
     *
     * @param selectMajorEvaluationProcessListBO 专业名称
     * @return 此专业的所有评审流程
     */
    List<MajorEvaluationProcessVO> getMajorEvaluationByName(SelectMajorEvaluationProcessListBO selectMajorEvaluationProcessListBO);

    /**
     * 删除对应流程
     *
     * @param t 流程所属类型
     * @return 是否删除成功
     */
    Boolean deleteProcess(T t) throws NoSuchFieldException, IllegalAccessException;

    /**
     * 修改专业评审流程的评审状态
     *
     * @param majorEvaluationProcess 修改后的评审流程
     * @return 是否修改成功
     */
    Boolean changeMajorProcessStatus(MajorEvaluationProcess majorEvaluationProcess);

    /**
     * 修改课程评审流程的评审状态
     *
     * @param classEvaluationProcess 修改后的评审流程
     * @return 是否修改成功
     */
    Boolean changeClazzProcessStatus(ClassEvaluationProcess classEvaluationProcess);
}
