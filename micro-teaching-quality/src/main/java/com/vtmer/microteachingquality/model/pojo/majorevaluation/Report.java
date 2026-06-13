package com.vtmer.microteachingquality.model.pojo.majorevaluation;

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

import java.time.LocalDateTime;

/**
 * @author Hung
 */
@ApiModel("报告记录表，专门记录每个专业评审报告的信息")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Report extends Model<Report> {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Integer id;
    @ApiModelProperty("上传这个文件的用户Id")
    private Integer userId;
    @ApiModelProperty("上传这个文件的用户的学院")
    private String college;
    @ApiModelProperty("所属的专业Id")
    private Integer majorId;
    @ApiModelProperty("所属的专业")
    private String major;
    @ApiModelProperty("所属的评审流程Id")
    private Long majorEvaluationProcessId;
    @ApiModelProperty("上传文件的文件名")
    private String fileName;
    @ApiModelProperty("文件加密路径")
    private String path;
    @ApiModelProperty("url")
    private String url;
    @ApiModelProperty("合格状态")
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Report(Integer userId, Integer majorId, Long majorEvaluationProcessId, String fileName, String path) {
        this.userId = userId;
        this.majorId = majorId;
        this.majorEvaluationProcessId = majorEvaluationProcessId;
        this.fileName = fileName;
        this.path = path;
    }
}