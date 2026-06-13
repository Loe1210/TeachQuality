package com.vtmer.microteachingquality.service;

import com.vtmer.microteachingquality.model.dto.EvaluationUserDTO;
import com.vtmer.microteachingquality.model.vo.EvaluationUserVO;

import java.util.List;
import java.util.Map;

public interface RoleService {

    // 查看
    List<EvaluationUserVO> getMajorEvaluationUser(Map<String, Object> map);

    // 新建（分配）
    Boolean createMajorEvaluationUser(String majorName, Integer userId);

    Boolean createMajorReviewEvaluationUser(String majorName, Integer userId);

    Boolean createClazzEvaluationUser(String clazzName, Integer userId);

    Boolean createClazzReviewEvaluationUser(String clazzName, Integer userId);

    // 修改
    Boolean updateMajorEvaluationUser(EvaluationUserDTO evaluationUserDTO);

    Boolean updateMajorReviewEvaluationUser(EvaluationUserDTO evaluationUserDTO);

    Boolean updateClazzEvaluationUser(EvaluationUserDTO evaluationUserDTO);

    Boolean updateClazzReviewEvaluationUser(EvaluationUserDTO evaluationUserDTO);

    // 删除
    Boolean deleteEvaluationUser(Integer evaluationUserId, Integer kind);
}
