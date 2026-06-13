package com.vtmer.microteachingquality.controller;

import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.model.dto.EvaluationUserDTO;
import com.vtmer.microteachingquality.model.vo.EvaluationUserVO;
import com.vtmer.microteachingquality.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Colin_Knight
 * @create 2023/10/19 16:04
 */
@RestController
@RequestMapping("/role")
@Slf4j
@Api(tags = "用户权限设置模块")
@Validated
@PreAuthorize("hasAnyAuthority('all')")
public class RoleController {

    @Resource
    RoleService roleService;

    @ApiOperation("根据id查看角色的所有评审权限")
    @GetMapping("/getAllInfo/{userid}")
    public ResponseMessage<List<EvaluationUserVO>> gerAllInfoById(@PathVariable String userid) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userid);
        return ResponseMessage.newSuccessInstance(roleService.getMajorEvaluationUser(map));
    }

    @ApiOperation("根据用户名查看角色的所有评审权限")
    @GetMapping("/getAllInfo/username")
    public ResponseMessage<List<EvaluationUserVO>> gerAllInfoByName(@RequestParam String username) {
        Map<String, Object> map = new HashMap<>();
        map.put("real_name", username);
        return ResponseMessage.newSuccessInstance(roleService.getMajorEvaluationUser(map));
    }

    @ApiOperation("新建评审权限")
    @PostMapping
    public ResponseMessage<String> createEvaluationUser(
            @NotNull(message = "用户id为空") @RequestParam Integer userId,
            @NotNull(message = "项目名为空") @RequestParam String insertName,
            @NotNull(message = "种类为空") @RequestParam Integer kind
    ) {
        Boolean res = createEvaluationUserByKind(userId, insertName, kind);
        return res ? ResponseMessage.newSuccessInstance("创建成功") : ResponseMessage.newErrorInstance("创建失败");
    }

    @Deprecated
    @ApiOperation("兼容旧版：新建评审权限")
    @GetMapping("/create")
    public ResponseMessage<String> createEvaluationUserLegacy(
            @NotNull(message = "用户id为空") @RequestParam Integer userId,
            @NotNull(message = "项目名为空") @RequestParam String insertName,
            @NotNull(message = "种类为空") @RequestParam Integer kind
    ) {
        return createEvaluationUser(userId, insertName, kind);
    }

    @ApiOperation("修改评审权限")
    @PostMapping("/update")
    public ResponseMessage<String> updateEvaluationUser(
            @NotNull(message = "内容不准为空") @RequestBody EvaluationUserDTO evaluationUserDTO) {
        Boolean res;
        if (evaluationUserDTO.getKind() == 0) {
            res = roleService.updateMajorEvaluationUser(evaluationUserDTO);
        } else if (evaluationUserDTO.getKind() == 1) {
            res = roleService.updateMajorReviewEvaluationUser(evaluationUserDTO);
        } else if (evaluationUserDTO.getKind() == 2) {
            res = roleService.updateClazzEvaluationUser(evaluationUserDTO);
        } else if (evaluationUserDTO.getKind() == 3) {
            res = roleService.updateClazzReviewEvaluationUser(evaluationUserDTO);
        } else res = false;

        return res ? ResponseMessage.newSuccessInstance("修改成功") : ResponseMessage.newErrorInstance("修改失败");
    }

    @ApiOperation("删除评审权限")
    @DeleteMapping("/{evaluationUserId}")
    public ResponseMessage<String> deleteEvaluationUser(
            @NotNull(message = "种类为空") @RequestParam Integer kind, @PathVariable Integer evaluationUserId) {
        return roleService.deleteEvaluationUser(evaluationUserId, kind) ?
                ResponseMessage.newSuccessInstance("删除成功") :
                ResponseMessage.newErrorInstance("删除失败");
    }

    @Deprecated
    @ApiOperation("兼容旧版：删除评审权限")
    @GetMapping("/delete/{evaluationUserId}")
    public ResponseMessage<String> deleteEvaluationUserLegacy(
            @NotNull(message = "种类为空") @RequestParam Integer kind, @PathVariable Integer evaluationUserId) {
        return deleteEvaluationUser(kind, evaluationUserId);
    }

    private Boolean createEvaluationUserByKind(Integer userId, String insertName, Integer kind) {
        if (kind == 0) {
            return roleService.createMajorEvaluationUser(insertName, userId);
        }
        if (kind == 1) {
            return roleService.createMajorReviewEvaluationUser(insertName, userId);
        }
        if (kind == 2) {
            return roleService.createClazzEvaluationUser(insertName, userId);
        }
        if (kind == 3) {
            return roleService.createClazzReviewEvaluationUser(insertName, userId);
        }
        return false;
    }
}
