package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author HJW
 */
@Data
@ApiModel("负责人获取上传的文件信息")
@AllArgsConstructor
@NoArgsConstructor
public class MajorArchiveFileResult {

    @ApiModelProperty("文件id")
    private Integer fileId;

    @ApiModelProperty("文件名字")
    private String fileName;

    @ApiModelProperty("文件路径")
    private String filePath;

    @ApiModelProperty("上传时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("所属专业名字")
    private String majorName;

    @ApiModelProperty("所属批次名字")
    private String batchName;

}
