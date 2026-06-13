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
 * @author HJW
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "专业归档文件表")
public class MajorArchiveFile extends Model<MajorArchiveFile> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 文件名字
     */
    @ApiModelProperty(value = "文件名字")
    private String fileName;
    /**
     * 文件加密路径
     */
    @ApiModelProperty(value = "文件加密路径")
    private String path;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty("所属专业Id")
    private Integer majorId;

    @ApiModelProperty("所属的专业归档流程id")
    private Long batchId;

    @ApiModelProperty("所属的批次名")
    private String batchName;

    @ApiModelProperty("上传人id")
    private Integer userId;

    @ApiModelProperty("是否删除")
    private Integer isDelete;
}