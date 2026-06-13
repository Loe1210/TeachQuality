package com.vtmer.microteachingquality.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户类")
@ToString
public class User extends Model<User> {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userName;

    private String userPwd;

    private String realName;

    private String userType;

    private String userBelong;

    private Integer isClazz;

    private String email;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<String> roles;

    public User(Integer id, String userName, String userPwd, String realName, String userType, String userBelong, Integer isClazz, String email, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.userName = userName;
        this.userPwd = userPwd;
        this.realName = realName;
        this.userType = userType;
        this.userBelong = userBelong;
        this.isClazz = isClazz;
        this.email = email;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}