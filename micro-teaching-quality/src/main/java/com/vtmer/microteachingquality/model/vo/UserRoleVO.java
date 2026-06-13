package com.vtmer.microteachingquality.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Hung
 * @date 2022/12/5 19:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleVO {
    List<String> roleList;
    Integer userId;
}
