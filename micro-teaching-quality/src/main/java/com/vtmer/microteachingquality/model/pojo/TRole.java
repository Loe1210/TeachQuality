package com.vtmer.microteachingquality.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author : Gking
 * @date : 2022-07-20 13:13
 **/
@TableName("t_role")
@Data
@NoArgsConstructor
public class TRole {
    @TableId(type = IdType.AUTO)
    private String id;
    private String roleName;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Character status;

    public TRole(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
        this.status = 0;
    }
}
