package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Hung
 * @date 2022/8/27 0:10
 */
@ApiModel(value = "点进课程的课程评审流程 简易展示信息")
@Data
public class ClazzEvaluationProcessSimpleInfoVO {
    private String id;
    @ApiModelProperty("创建此评审流程的用户名")
    private String creatorName;
    @ApiModelProperty("当前评审流程状态 五个阶段 流程创建 负责人材料上传 专家评审完成 组长评审完成 专家小组评审完成 流程结束")
    private String status;
    @ApiModelProperty("流程创建时间")
    private LocalDateTime createTime;
}
