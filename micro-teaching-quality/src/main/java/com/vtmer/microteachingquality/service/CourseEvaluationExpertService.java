package com.vtmer.microteachingquality.service;


import com.vtmer.microteachingquality.model.bo.SubmitEvaluationRecordBO;
import com.vtmer.microteachingquality.model.dto.ClazzInJudgeResultDTO;
import com.vtmer.microteachingquality.model.vo.ClazzFinishResult;
import com.vtmer.microteachingquality.model.vo.FinishedClazzReviewVO;
import com.vtmer.microteachingquality.model.vo.GetClazzFilesResult;

import java.util.List;

/**
 * @author Hung
 * @date 2022/5/25 0:23
 */
public interface CourseEvaluationExpertService {

    /**
     * 获取评审课程报告信息
     *
     * @param status   评审状态
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 返回课程报告信息
     */
    FinishedClazzReviewVO getCourseMessageOfEvaluationExpert(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取评审信息
     *
     * @param clazzId 课程id
     * @return 返回评审信息
     */
    List<ClazzInJudgeResultDTO> getClazzInJudgeByClazzType(Integer clazzId);


    /**
     * 专家评审获取自己的评审信息
     *
     * @param clazzEvaluationProcessId 课程评审流程id
     * @param userId                   专家id
     * @return 返回评审信息
     */
    ClazzFinishResult getSingleClazzFinishReview(Long clazzEvaluationProcessId, Integer userId);

    /**
     * 课程评审专家提交评审记录
     *
     * @param submitEvaluationRecordBO 评审记录信息
     * @return 是否插入成功
     */
    Boolean insertEvaluationRecord(SubmitEvaluationRecordBO submitEvaluationRecordBO);

    /**
     * 根据课程名字返回文件列表
     *
     * @param clazzEvaluationProcessId 课程评审流程id
     * @return 返回文件列表
     */
    List<GetClazzFilesResult> getAllEvaluationFiles(Long clazzEvaluationProcessId);

    /**
     * 插入空白数据用接口
     *
     * @param tableName 表名
     * @param size      插入数据量
     * @return 是否插入成功
     */
    Integer insertEmptyData(String tableName, Integer size);
}
