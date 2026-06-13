package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("专业评审 专业负责人上传的报告 返回信息")
public class MajorEvaluationGetFileResult {
    @ApiModelProperty("报告id")
    private Integer id;
    @ApiModelProperty("上传用户id")
    private Integer userId;
    @ApiModelProperty("文件名字")
    private String fileName;
    @ApiModelProperty("文件加密路径")
    private String path;

}
