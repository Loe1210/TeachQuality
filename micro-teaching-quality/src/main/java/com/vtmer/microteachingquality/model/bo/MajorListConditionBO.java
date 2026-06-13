package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Hung
 * @date 2022/11/9 16:18
 */
@Data
@ApiModel("查询专业条件模型")
public class MajorListConditionBO {
    @ApiModelProperty("关键词")
    String keywords;
}
