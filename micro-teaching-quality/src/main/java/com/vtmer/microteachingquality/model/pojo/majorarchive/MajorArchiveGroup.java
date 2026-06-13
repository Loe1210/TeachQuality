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
@ApiModel(value = "专业归档小组表")
public class MajorArchiveGroup extends Model<MajorArchiveGroup> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "评审小组组长id")
    private Integer leaderId;

    @ApiModelProperty(value = "小组组内消息")
    private String information;

    @ApiModelProperty(value = "小组权限范围")
    private String authority;

    @ApiModelProperty(value = "小组名字")
    private String groupName;

}
