package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author Hung
 * @date 2022/11/9 15:45
 */
@ApiModel("课程查询条件")
@Data
public class ClazzListConditionBO {
    String keywords;

}
