package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * @author HJW
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("获取专业归档课程名字、id和文件列表")
public class MajorInfoResult {
    @ApiModelProperty("专业名字")
    private String majorName;

    @ApiModelProperty("专业id")
    private Integer majorId;

    @ApiModelProperty("批次名字")
    private String batchName;

    @ApiModelProperty("文件列表")
    private List<FileInfoResult> fileInfoList;
}
