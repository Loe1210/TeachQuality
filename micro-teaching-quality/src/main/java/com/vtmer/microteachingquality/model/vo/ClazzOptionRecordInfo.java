package com.vtmer.microteachingquality.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Colin_Knight
 * @create 2023/9/12 21:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClazzOptionRecordInfo {

    private String major;

    private String classname;

    private String username;

    private String clazzAdvantage;

    private String clazzProblem;

    private String clazzAdvice;

    private String remark;

}
