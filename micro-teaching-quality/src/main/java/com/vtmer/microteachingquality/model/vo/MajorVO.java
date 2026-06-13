package com.vtmer.microteachingquality.model.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Hung
 * @date 2022/8/10 1:25
 */
@Data
public class MajorVO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 专业类型
     */
    @ApiModelProperty(value = "专业类型")
    private String type;

    /**
     * 专业名称
     */
    @ApiModelProperty(value = "专业名称")
    private String name;

    /**
     * 专业所属的学院
     */
    @ApiModelProperty(value = "专业所属的学院")
    private String college;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
