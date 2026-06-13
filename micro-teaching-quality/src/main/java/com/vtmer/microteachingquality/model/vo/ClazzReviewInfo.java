package com.vtmer.microteachingquality.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Colin_Knight
 * @create 2023/12/11 23:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClazzReviewInfo {

    private Integer id;

    /**
     *
     */
    private Long evaluationId;

    /**
     *
     */
    private String reviewEvaluation;

    /**
     *
     */
    private String remark;

    private String evaluateYear;

}
