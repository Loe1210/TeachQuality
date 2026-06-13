package com.hung.microoauth2auth.entity;

import lombok.*;

import java.util.List;

/**
 * @author Hung
 * @date 2022/4/10 20:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPrincipalDTO {
    private Integer id;

    private String userName;

    private String realName;

    private String userType;

    private String userBelong;

    private Integer isClazz;

    private List<String> roles;
}
