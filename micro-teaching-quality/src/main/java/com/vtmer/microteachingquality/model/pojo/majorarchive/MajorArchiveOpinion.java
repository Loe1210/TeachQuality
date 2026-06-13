package com.vtmer.microteachingquality.model.pojo.majorarchive;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * major_archive_opinion
 *
 * @author
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "专业归档评审建议表")
public class MajorArchiveOpinion extends Model<MajorArchiveOpinion> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 被评审的专业id
     */
    @ApiModelProperty(value = "被评审的批次id")
    private Long batchId;
    /**
     * 提交评审的用户id
     */
    @ApiModelProperty(value = "提交评审的用户id")
    private Integer userId;

    @ApiModelProperty(value = "专业id")
    private Integer majorId;

    /**
     * 专家意见
     */
    @ApiModelProperty(value = "专家意见")
    private String opinion;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    public MajorArchiveOpinion(Long batchId, Integer userId, String opinion) {
        this.batchId = batchId;
        this.userId = userId;
        this.opinion = opinion;
    }
}