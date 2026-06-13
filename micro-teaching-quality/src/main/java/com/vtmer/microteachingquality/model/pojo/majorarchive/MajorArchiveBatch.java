package com.vtmer.microteachingquality.model.pojo.majorarchive;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Hung
 * @date 2022/8/10 1:46
 */
@ApiModel(value = "专业归档批次表")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MajorArchiveBatch extends Model<MajorArchiveBatch> implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "批次名字")
    private String batchName;

    @ApiModelProperty(value = "所属专业id")
    private Integer majorId;

    @ApiModelProperty(value = "创建此专业评审流程的用户Id")
    private Integer creatorId;

    @ApiModelProperty("创建专业归档流程状态 0:未开始 1:已结束")
    private Integer createProcessStatus;
    @ApiModelProperty("专业归档负责人提交归档材料状态 0:未开始  1:进行中 2:已提交 3:被退回")
    private Integer principalMaterialStatus;
    @ApiModelProperty("专业归档专家评审状态 0:未开始  1:进行中 2:已提交")
    private Integer expertReviewStatus;
    @ApiModelProperty("项目流程结束状态 0:未开始 1:进行中 2:已结束 ")
    private Integer processEndStatus;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public MajorArchiveBatch(String batchName, Integer majorId, Integer creatorId) {
        this.batchName = batchName;
        this.majorId = majorId;
        this.creatorId = creatorId;
        this.createProcessStatus = 1;
        this.principalMaterialStatus = 1;
        this.expertReviewStatus = 0;
        this.processEndStatus = 0;
    }
}
