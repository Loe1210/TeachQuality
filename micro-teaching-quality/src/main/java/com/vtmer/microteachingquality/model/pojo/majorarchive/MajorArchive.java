package com.vtmer.microteachingquality.model.pojo.majorarchive;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @author HJW
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "专业评审表")
public class MajorArchive extends Model<MajorArchive> implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    /**
     * 创建评审的负责人id
     */
    @ApiModelProperty(value = "创建评审的负责人id")
    private Integer userId;
    /**
     * 用户负责的专业id
     */
    @ApiModelProperty(value = "用户负责的专业id")
    private Integer majorId;

    /**
     * 批次名
     */
    @ApiModelProperty(value = "批次名")
    private String batchName;


    @ApiModelProperty(value = "评审小组id")
    private Integer groupId;

    @ApiModelProperty(value = "评审状态id")
    private Integer statusId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
