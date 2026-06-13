package com.vtmer.microteachingquality.model.bo;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author Hung
 * @date 2023/2/19 19:14
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SelectMajorEvaluationListBO {

    @NotNull(message = "课程id为空")
    @ApiParam("课程ID")
    Integer majorId;
    @ApiParam("每页显示的条数")
    @NotNull(message = "页大小为空") Integer pageSize;
    @ApiParam("页码")
    @NotNull(message = "页码为空") Integer pageNum;


}
