package com.vtmer.microteachingquality.service;

import java.util.List;

public interface EvaluationProcessFileService {
    /**
     * 导出专业评审评估报告
     *
     * @param majorEvaluationProcessId 专业评审流程id
     * @return 储存路径
     */
    List<String> generateMajorEvaluationReport(Long majorEvaluationProcessId);

    /**
     * 导出课程评审评估报告
     *
     * @param clazzEvaluationProcessId 课程评审流程id
     * @return 储存路径
     */
    List<String> generateClazzEvaluationReport(Long clazzEvaluationProcessId);
}
