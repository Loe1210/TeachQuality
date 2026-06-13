package com.vtmer.microteachingquality.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author batstroke
 */
@Data
@Accessors(chain = true)
public class EvaluationUserVO {
    private String kind;
    private Integer userId;
    private String username;
    private Integer clazzOrMajorId;
    private String clazzOrMajorName;
    private Integer evaluationUserId;
}
