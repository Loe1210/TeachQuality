package com.vtmer.microteachingquality.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.hung.microoauth2commons.commonutils.api.Result;
import com.hung.microoauth2commons.commonutils.utils.RegexUtils;
import com.vtmer.microteachingquality.common.PageResponseMessage;
import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.common.constant.enums.UserType;
import com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.model.bo.ForgetPwdBO;
import com.vtmer.microteachingquality.model.bo.ReviseUserPermissionBO;
import com.vtmer.microteachingquality.model.bo.UserInsertBO;
import com.vtmer.microteachingquality.model.dto.*;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.UserRole;
import com.vtmer.microteachingquality.model.vo.UserRoleVO;
import com.vtmer.microteachingquality.service.TRoleService;
import com.vtmer.microteachingquality.service.UserRoleService;
import com.vtmer.microteachingquality.service.UserService;
import com.vtmer.microteachingquality.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

import static com.hung.microoauth2commons.commonutils.utils.RedisConstants.FORGET_CODE_KEY;
import static com.hung.microoauth2commons.commonutils.utils.RedisConstants.USER_CODE_KEY;
import static com.vtmer.microteachingquality.common.ResponseMessage.newSuccessInstance;

/**
 * @author eeatem
 */
@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private TRoleService tRoleService;

    @ApiOperation("获取当前登录账号的信息")
    @GetMapping("/currentUserInfo")
    @PreAuthorize("hasAnyAuthority('all','clazz_principal','clazz_expert_leader','clazz_expert','major_archive_expert'," +
            "'major_archive_principal','major_evaluation_principal','major_evaluation_expert','major_evaluation_expert_leader')")
    public ResponseMessage<UserPrincipalDTO> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String jsonStr = JSONUtil.toJsonStr(principal);
        UserPrincipalDTO userPrincipalDTO = JSON.parseObject(jsonStr, UserPrincipalDTO.class);
        return ResponseMessage.newSuccessInstance(userPrincipalDTO);
    }

    @ApiOperation(value = "修改密码")
    @PutMapping("/password")
    @PreAuthorize("hasAnyAuthority('all','clazz_principal','clazz_expert_leader','clazz_expert','major_archive_expert'," +
            "'major_archive_principal','major_evaluation_principal','major_evaluation_expert','major_evaluation_expert_leader')")
    public ResponseMessage<?> updatePwd(@RequestBody @Validated UpdatePwdDTO updatePwdDTO) {
        // 获取当前登陆用户对象
        User loginUser = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);

        if (!updatePwdDTO.getNewPwd().equals(updatePwdDTO.getNewPwdConfirm())) {
            return ResponseMessage.newErrorInstance("两次新密码输入不一致，修改密码失败");
        }
        if (bCryptPasswordEncoder.matches(updatePwdDTO.getOldPwd(), loginUser.getUserPwd())) {
            if (userService.updatePwd(loginUser, updatePwdDTO.getNewPwdConfirm()) > 0) {
                return newSuccessInstance("修改密码成功");
            }
        } else {
            return ResponseMessage.newErrorInstance("原始密码输入错误，修改密码失败");
        }
        return ResponseMessage.newErrorInstance("修改密码失败");
    }

    @ApiOperation(value = "管理员 分页获取全部账号信息")
    @GetMapping("/userInfo")
    @PreAuthorize("hasAnyAuthority('all')")
    public ResponseMessage<?> listUserInfo(
            @ApiParam("查询页数(第几页)") @RequestParam @NotNull(message = "页号为空") @Min(value = 1, message = "页数最小为1") Integer pageNum,
            @ApiParam("单页查询数量") @RequestParam @NotNull(message = "页大小为空") @Min(value = 1) Integer pageSize) {
        return newSuccessInstance(PageResponseMessage.restPage(userService.listUserInfo(pageNum, pageSize)));
    }

    @ApiOperation(value = "管理员 通过用户名分页获取账号信息")
    @GetMapping("/userInfoByRealName")
    @PreAuthorize("hasAnyAuthority('all')")
    public ResponseMessage<?> listUserInfoByRealName(
            @ApiParam("查询页数(第几页)") @RequestParam @NotNull(message = "页号为空") @Min(value = 1, message = "页数最小为1") Integer pageNum,
            @ApiParam("单页查询数量") @RequestParam @NotNull(message = "页大小为空") @Min(value = 1) Integer pageSize,
            @ApiParam("用户名") @RequestParam(required = false, value = "realName") String realName) {
        return newSuccessInstance(PageResponseMessage.restPage(userService.listUserInfoByRealName(pageNum, pageSize, realName)));
    }

    @ApiOperation(value = "管理员 注销账户")
    @DeleteMapping("/deleteUserByUserId")
    @PreAuthorize("hasAnyAuthority('all')")
    public ResponseMessage<?> deleteUserByUserId(@ApiParam("用户账号id") @PathVariable("userId") Integer userId) {
        User loginUser = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
        if (!loginUser.getUserType().equals(UserType.SCHOOL.getType())) {
            return ResponseMessage.newErrorInstance("非学校账号，无法重置用户密码");
        }
        if (userService.deleteUserByUserId(userId)) {
            return newSuccessInstance("注销成功");
        } else {
            return ResponseMessage.newErrorInstance("注销失败");
        }
    }

    @ApiOperation(value = "(学校账号)重置指定用户密码", notes = "默认与用户名相同")
    @PutMapping("/password/{userId}")
    @PreAuthorize("hasAnyAuthority('all')")
    public ResponseMessage<?> updatePwdByUserId(@ApiParam("用户账号id") @PathVariable("userId") Integer userId) {
        User loginUser = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
        if (!loginUser.getUserType().equals(UserType.SCHOOL.getType())) {
            return ResponseMessage.newErrorInstance("非学校账号，无法重置用户密码");
        }
        if (userService.updatePwdByUserId(userId) > 0) {
            return newSuccessInstance("账号密码重置成功");
        } else {
            return ResponseMessage.newErrorInstance("账号密码重置失败");
        }
    }


    @ApiOperation(value = "分页根据所属部门或指定专业获取账号信息")
    @GetMapping("/userInfoByUserBelong")
    public ResponseMessage<?> listUserInfoByUserBelong(@ApiParam(value = "所属部门或指定专业", required = true)
                                                       @NotBlank(message = "所属部门或指定专业不能为空")
                                                       @RequestParam(value = "userBelong") String userBelong,
                                                       @ApiParam("查询页数(第几页)") @Param(value = "pageNum") Integer pageNum,
                                                       @ApiParam("单页查询数量") @Param(value = "pageSize") Integer pageSize) {
        if (pageNum != null && pageNum != null && pageNum > 0 && pageSize > 0) {
            PageHelper.startPage(pageNum, pageSize);
            return newSuccessInstance(PageResponseMessage.restPage(userService.listUserInfoByUserBelong(userBelong)));
        } else {
            return newSuccessInstance(userService.listUserInfoByUserBelong(userBelong));
        }
    }


    @ApiOperation(value = "发送验证码")
    @PostMapping("/send")
    public Result<String> sendCode(@RequestParam String email) {
        if (userService.sendCode(email)) {
            return Result.success("发送成功");
        } else {
            return Result.failed("发送失败，请稍后再试");
        }
    }

    @ApiOperation(value = "忘记密码发送验证码验证")
    @PostMapping("/sendForget")
    public Result<String> sendCodeWithForgetting(@RequestParam String email) {
        if (userService.sendCodeWithForgetting(email)) {
            return Result.success("发送成功");
        } else {
            return Result.failed("发送失败，请稍后再试");
        }
    }

    @ApiOperation(value = "创建注册账号")
    @PostMapping("/createAccount")
    public Result<String> createAccount(@RequestBody @Validated UserInsertBO insertUserDTO) {
        UserDTO userDTO = new UserDTO();
        //检验邮箱格式是否正确
        String email = insertUserDTO.getEmail();
        if (RegexUtils.isEmailInvalid(email)) {
            return Result.failed("邮箱格式错误");
        }
        //检验邮箱是否存在，存在返回错误
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        QueryWrapper<User> email1 = queryWrapper.eq("email", email);
        if ((userService.getOne(email1)) != null) {
            return Result.failed("该邮箱已经被绑定！！！");
        }
        //确认两次密码输入是否一致
        if (!Objects.equals(insertUserDTO.getUserPwd(), insertUserDTO.getConfirmPassword())) {
            return Result.failed("两次密码输入不一致！！！请重新输入密码！");
        }

        //从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(USER_CODE_KEY + email);
        String code = insertUserDTO.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.failed("验证码错误");
        }
        BeanUtils.copyProperties(insertUserDTO, userDTO);
        userDTO.setUserType("课程负责人");
        // 原始密码默认与账号名称相同
        //user.setUserPwd(bCryptPasswordEncoder.encode(insertUserDTO.getUserName()));
        return userService.createUser(userDTO) ? Result.success("注册成功") : Result.failed("注册失败");
    }


    @ApiOperation("忘记密码重置密码")
    @PostMapping("/forgetPassword")
    public Result<String> forgetPasswordAndReset(@RequestBody ForgetPwdBO forgetPwdBO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        QueryWrapper<User> userQueryWrapper = queryWrapper.eq("user_name", forgetPwdBO.getUserAccount())
                .eq("real_name", forgetPwdBO.getName())
                .eq("email", forgetPwdBO.getEmail());
        if (userService.getOne(userQueryWrapper) == null) {
            return Result.failed("该用户不存在！");
        }
        //从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(FORGET_CODE_KEY + forgetPwdBO.getEmail());
        String code = forgetPwdBO.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.failed("验证码错误");
        }
        //密码两次输入一致
        if (!Objects.equals(forgetPwdBO.getConfirmPassword(), forgetPwdBO.getPassword())) {
            return Result.failed("两次密码输入不一致，请重新输入！！！");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        UpdateWrapper<User> set = userUpdateWrapper.eq("user_name", forgetPwdBO.getUserAccount())
                .eq("real_name", forgetPwdBO.getName())
                .eq("email", forgetPwdBO.getEmail())
                .set("user_pwd", bCryptPasswordEncoder.encode(forgetPwdBO.getPassword()));

        boolean update = userService.update(set);
        if (update) {
            return Result.success("重置密码成功");
        } else {
            return Result.failed("重置密码失败！");
        }
    }


    @ApiOperation("修改个人信息")
    @PostMapping("/update")
    public Result<String> update(UpdateUserDTO updateUserDTO) {
        try {
            User user = UserUtil.getCurrentUser();
            BeanUtils.copyProperties(updateUserDTO, user);
            Boolean aBoolean = userService.updateUserInfo(user);
            if (!aBoolean) {
                return Result.failed("修改失败！");
            }
            return Result.success("修改成功");
        } catch (Exception e) {
            throw new CustomException("暂时无法修改，如有需要请联系管理员");
        }
    }

    @ApiOperation("查询个人详细信息")
    @GetMapping("/detail")
    public Result<UpdateUserDTO> detail(Integer id) {
        User user = userService.getById(id);
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        BeanUtils.copyProperties(user, updateUserDTO);
        return Result.success(updateUserDTO);
    }

    @ApiOperation("超级管理员/评审专家通过用户姓名查询用户详细信息")
    @GetMapping("/UserInfo")
    public Result<List<UserInfoDTO>> detail(String name) {
        List<UserInfoDTO> userByName = userService.findUserByName(name);
        return Result.success(userByName);
    }


    @ApiOperation("获取用户的角色")
    @GetMapping("/role")
    public Result<UserRoleVO> getUserRole(@NotNull @Min(value = 1) @RequestParam Integer userId) {
        List<String> userRole = userRoleService.getUserRole(userId);
        return Result.success(new UserRoleVO(userRole, userId));
    }


    @ApiOperation("超级管理员更改用户角色")
    @PostMapping("/permission")
    public Result<String> reviseUserPermission(@Validated @NotNull @RequestBody ReviseUserPermissionBO reviseUserPermissionBO) {
        //查看本人是否有管理员权限
        if (!UserUtil.isRoleAvailable(UserTypeConstant.MASTER)) {
            throw new CustomException("当前用户不是管理员，请联系相关老师进行处理");
        }
        if (!userRoleService.reviseUserRole(reviseUserPermissionBO)) {
            return Result.failed("授予权限失败！");
        }
        return Result.success("授予权限成功！");
    }

    /**
     * @param ownerId    评审组长用户id
     * @param userId     分配角色的用户id
     * @param permission 分配角色名字
     * @return
     */
    @ApiOperation("课程/专业 评审专家组长选定评审专家角色")
    @PostMapping("/permissionByCaptain")
    public Result<String> givePermissionByCaptain(Integer ownerId, Integer userId, String permission) {
        //查看本人是否有专家组长权限
        boolean captainPermissionHave = userRoleService.isCaptainPermissionHave(ownerId);
        if (!captainPermissionHave) {
            return Result.failed("没有评审组长权限！！！");
        }
        //根据传进来的权限名，查看权限对应的id
        String permissionId = tRoleService.getPermissionId(permission);
        //查看选定的用户是否已经有该permission权限
        boolean permissionHave = userRoleService.isPermissionHave(userId, permissionId, String.valueOf(ownerId));
        if (permissionHave) {
            return Result.failed("该用户已经拥有评审专家权限！");
        }
        //授予权限
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(Integer.valueOf(permissionId));
        User user = userService.getById(ownerId);
        userRole.setCreatorId(String.valueOf(user.getId()));
        boolean save = userRole.insert();
        if (!save) {
            return Result.failed("授予权限失败！");
        }
        return Result.success("授予权限成功！");
    }

    @GetMapping("/passwordCheck")
    @ApiOperation("检查是否需要更改密码 （密码和用户名重复）")
    public Result<String> isNeedToRevisePassword() {
        return userService.isNeedToRevisePassword() ? Result.success("需要更改密码") : Result.success("不需要更改密码");
    }

}
