package com.vtmer.microteachingquality.model.pojo.majorarchive;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author HJW
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "专业归档管理信息表")
public class MajorArchiveManageInfo extends Model<MajorArchiveManageInfo> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 管理者id
     */
    @ApiModelProperty(value = "管理者id")
    private Integer userId;
    /**
     * 被管理的专业id
     */
    @ApiModelProperty(value = "被管理的专业id")
    private Integer majorId;
    /**
     * 批次名字
     */
    @ApiModelProperty(value = "批次名字")
    private String batchName;
    /**
     * 该批次的评审状态：1已经评审，0未评审
     */
    @ApiModelProperty(value = "该批次的评审状态：1已经评审，0未评审")
    private String status;
}