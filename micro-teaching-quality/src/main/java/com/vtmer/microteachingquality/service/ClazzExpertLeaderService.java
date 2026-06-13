package com.vtmer.microteachingquality.service;

import com.vtmer.microteachingquality.model.vo.ClazzLeaderGetMembersEvaluationInfoResult;
import com.vtmer.microteachingquality.model.vo.ClazzLeaderGetNotEvaluateClazzInfoResult;

import java.util.List;

/**
 * 课程评审专家组长Service
 */

public interface ClazzExpertLeaderService {

    /**
     * 课程组长获取待评价的课程信息
     */
    ClazzLeaderGetNotEvaluateClazzInfoResult getNotEvaluateClazzInfo(Integer startPage, Integer pageSize);


    /**
     * 课程组长账号已评价的课程
     */
    //不需要了

    /**
     * 课程组长账号下属组员的评价指标以及对应的课程的信息
     */
    List<ClazzLeaderGetMembersEvaluationInfoResult> getMembersEvaluationInfo();


}
