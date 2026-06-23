package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("文件对象绑定请求")
public class FileObjectBindBO {

    @NotNull(message = "文件对象ID不能为空")
    @Min(value = 1, message = "文件对象ID非法")
    @ApiModelProperty("文件服务生成的文件对象ID")
    private Long fileObjectId;
}
