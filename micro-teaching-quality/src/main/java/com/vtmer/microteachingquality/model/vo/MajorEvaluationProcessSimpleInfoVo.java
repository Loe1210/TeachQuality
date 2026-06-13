package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Colin_Knight
 * @create 2023/5/5 0:10
 */
@ApiModel(value = "点进专业评审，查看流程，简易展示信息")
@Data
public class MajorEvaluationProcessSimpleInfoVo {

    private String id;

    private String creatorName;

    private String status;

    private LocalDateTime createTime;

}
