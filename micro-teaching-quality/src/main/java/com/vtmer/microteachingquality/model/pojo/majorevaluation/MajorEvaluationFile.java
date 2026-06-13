package com.vtmer.microteachingquality.model.pojo.majorevaluation;

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
 * @author Colin_Knight
 * @create 2023/5/8 16:24
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "专业评审文件表")
public class MajorEvaluationFile extends Model<MajorEvaluationFile> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("所属专业Id")
    private Integer majorId;


    @ApiModelProperty("上传人id")
    private Integer userId;

    @ApiModelProperty("课程评价流程id")
    private Long majorEvaluationProcessId;

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


    @ApiModelProperty("是否删除")
    private Integer isDelete;

    public MajorEvaluationFile(Integer userId, String fileName, Integer majorId, Long majorEvaluationProcessId, String path) {
        this.userId = userId;
        this.fileName = fileName;
        this.majorId = majorId;
        this.majorEvaluationProcessId = majorEvaluationProcessId;
        this.path = path;
    }

    public MajorEvaluationFile(Integer majorId, Integer userId, Long majorEvaluationProcessId, String fileName, String path) {
        this.majorId = majorId;
        this.userId = userId;
        this.majorEvaluationProcessId = majorEvaluationProcessId;
        this.fileName = fileName;
        this.path = path;
    }

}
