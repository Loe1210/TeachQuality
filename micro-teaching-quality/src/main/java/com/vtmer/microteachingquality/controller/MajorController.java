package com.vtmer.microteachingquality.controller;

import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant;
import com.vtmer.microteachingquality.model.bo.MajorBO;
import com.vtmer.microteachingquality.model.bo.SelectMajorListBO;
import com.vtmer.microteachingquality.model.vo.MajorVO;
import com.vtmer.microteachingquality.service.MajorService;
import com.vtmer.microteachingquality.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Hung
 * @date 2022/8/10 0:58
 */
@RequestMapping("/major")
@Api(tags = "专业模块 接口")
@Slf4j
@RestController
@PreAuthorize("hasAnyAuthority('all','major_archive_expert','major_archive_principal','major_evaluation_principal'," +
        "'major_evaluation_expert','major_evaluation_expert_leader','major_principal')")
public class MajorController {

    @Resource
    private MajorService majorService;


    @ApiOperation("用户新建专业")
    @PostMapping("/major")
    public ResponseMessage<String> createNewMajor(@Validated MajorBO majorBO) {
        if (!majorBO.getCollege().matches(".*学院$")) {
            return ResponseMessage.newErrorInstance("学院名称必须为“xx学院”");
        }
        if (majorBO.getName().matches(".*\\d{4}.*")) {
            return ResponseMessage.newErrorInstance("名称尽量不要带有年级");
        }
        return majorService.createNewMajor(majorBO) ? ResponseMessage.newSuccessInstance("用户创建专业成功") : ResponseMessage.newErrorInstance("用户创建专业失败");
    }

    @ApiOperation("用户查询专业")
    @PostMapping("/majorList")
    public ResponseMessage<List<MajorVO>> getMajorList(@RequestBody @Validated SelectMajorListBO selectMajorListBO) {
        if (UserUtil.isRoleAvailable(UserTypeConstant.MAJOR_EVALUATION_EXPERT) || UserUtil.isRoleAvailable(UserTypeConstant.MAJOR_EVALUATION_PRINCIPAL_LEADER)) {
            return ResponseMessage.newSuccessInstance(majorService.getMajorsByRole(selectMajorListBO));
        }
        return ResponseMessage.newSuccessInstance(majorService.getMajorList(selectMajorListBO));
    }


}
