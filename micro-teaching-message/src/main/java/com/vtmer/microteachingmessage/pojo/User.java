package com.vtmer.microteachingmessage.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Hung
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}