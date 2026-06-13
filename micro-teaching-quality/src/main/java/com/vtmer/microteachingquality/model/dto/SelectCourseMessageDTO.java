package com.vtmer.microteachingquality.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("")
public class SelectCourseMessageDTO {

    private Integer pageNum;

    private Integer pageSize;

    private String name;
}
