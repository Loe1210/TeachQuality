package com.vtmer.microteachingquality.model.bo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Hung
 * @date 2022/12/5 19:17
 */
@Data
public class ReviseUserPermissionBO {
    @NotNull(message = "用户id为空")
    Integer userId;
    @NotEmpty(message = "角色列表为空")
    List<String> roles;
}
