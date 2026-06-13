package com.vtmer.microteachingquality.service;

import com.vtmer.microteachingquality.model.vo.MajorEvaluationResult;

/**
 * @author Colin_Knight
 * @create 2023/5/9 22:23
 */
public interface MajorEvaluationRecodeService {

    MajorEvaluationResult getSingleMajorFinishReview(Long majorEvaluationProcessId, Integer userId);


    MajorEvaluationResult getLeaderMajorFinishReview(Long majorEvaluationProcessId, Integer userId);


}
