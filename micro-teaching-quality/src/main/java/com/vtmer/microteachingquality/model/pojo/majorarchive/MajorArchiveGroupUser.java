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
@ApiModel(value = "专业归档小组成员表")
public class MajorArchiveGroupUser extends Model<MajorArchiveGroupUser> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 评审小组人员id
     */
    @ApiModelProperty(value = "评审小组人员id")
    private Integer userId;

    /**
     * 评审小组id
     */
    @ApiModelProperty(value = "评审小组id")
    private Integer groupId;


}
