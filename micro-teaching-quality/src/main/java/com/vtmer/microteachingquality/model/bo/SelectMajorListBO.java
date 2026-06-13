package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Hung
 * @date 2022/11/9 13:12
 */
@ApiModel("查询专业列表 条件模型")
@Data
public class SelectMajorListBO {
    @ApiModelProperty("查询条件")
    MajorListConditionBO filter;
    @ApiModelProperty("每页显示的条数")
    @NotNull(message = "页大小为空")
    Integer pageSize;
    @ApiModelProperty("页码")
    @NotNull(message = "页码为空")
    Integer pageNum;
}
