package com.vtmer.microteachingquality.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HJW
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel("获取小组信息传输对象")
public class GroupInfoResult {
    @ApiModelProperty("小组id")
    private Integer groupId;

    @ApiModelProperty("小组名称")
    private String groupName;

    @ApiModelProperty("小组成员")
    private List<UserInfoResult> userInfoList;

    @ApiModelProperty("小组组长")
    private UserInfoResult groupLeader;
}
