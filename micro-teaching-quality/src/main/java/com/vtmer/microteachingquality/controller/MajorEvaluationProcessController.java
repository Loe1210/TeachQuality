package com.vtmer.microteachingquality.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.common.component.FileObjectReferenceService;
import com.vtmer.microteachingquality.common.constant.enums.UserType;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.model.bo.*;
import com.vtmer.microteachingquality.model.pojo.ReportTemplate;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationFile;
import com.vtmer.microteachingquality.model.vo.*;
import com.vtmer.microteachingquality.service.MajorEvaluationFileService;
import com.vtmer.microteachingquality.service.MajorEvaluationProcessService;
import com.vtmer.microteachingquality.service.MajorEvaluationRecodeService;
import com.vtmer.microteachingquality.service.ReportService;
import com.vtmer.microteachingquality.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hung
 * @date 2022/8/10 0:55
 */
@RequestMapping("/majorEvaluation")
@RestController
@Api(tags = "专业评审 模块相关接口")
@Slf4j
@PreAuthorize("hasAnyAuthority('all','major_evaluation_principal','major_evaluation_expert','major_evaluation_expert_leader','major_principal')")
public class MajorEvaluationProcessController {

    /**
     * 随机生成存储加密文件名(课程自评报告)的密钥
     * 固定密钥，不然会出bug
     */
    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);
    @Value("${report.template.path}")
    private String reportTemplatePath;
    @Value("${report.path}")
    private String reportPath;
    @Resource
    private ReportService reportService;

    @Resource
    private MajorEvaluationFileService majorEvaluationFileService;

    @Resource
    private MajorEvaluationProcessService majorEvaluationProcessService;


    @Resource
    private MajorEvaluationRecodeService majorEvaluationRecodeService;
    @Resource
    private FileObjectReferenceService fileObjectReferenceService;

    @ApiOperation("获取专业评审流程的状态信息")
    @GetMapping("/status")
    public ResponseMessage<MajorEvaluationProcessInfo> majorEvaluationProcessInfo(@NotNull(message = "课程评审流程Id为空") @RequestParam String majorEvaluationProcessId) {
        return ResponseMessage.newSuccessInstance(majorEvaluationProcessService.getEvaluationProcessInfo(Long.valueOf(majorEvaluationProcessId)));
    }


    @PostMapping("/evaluationProcess/{majorId}")
    @ApiOperation("创建评审流程")
    public ResponseMessage<String> createNewEvaluationProcess(@NotNull(message = "专业Id为空") @ApiParam(value = "课程id", required = true) @PathVariable("majorId") Integer majorId) {
        return majorEvaluationProcessService.createEvaluationProcess(majorId) ? ResponseMessage.newSuccessInstance("创建成功") : ResponseMessage.newErrorInstance("创建失败");
    }

    @DeleteMapping("/evaluationProcess/{majorEvaluationProcessId}")
    @ApiOperation("删除评审流程")
    public ResponseMessage<String> deleteEvaluationProcess(@ApiParam(value = "流程id", required = true) @PathVariable Long majorEvaluationProcessId) {
        return ResponseMessage.newSuccessInstance(majorEvaluationProcessService.deleteEvaluationProcess(majorEvaluationProcessId));
    }

    @GetMapping("/allEvaluationProcess/{majorId}")
    @ApiOperation("获取这个专业的所有评审流程")
    public ResponseMessage<List<MajorEvaluationProcessSimpleInfoVo>> getAllEvaluationProcesses(
            @Validated SelectMajorEvaluationListBO majorEvaluationListBO) {
        return ResponseMessage.newSuccessInstance(majorEvaluationProcessService.getEvaluationProcesses(majorEvaluationListBO));
    }

    @ApiOperation("第一阶段 (评审流程负责人)上传已填写的自评报告")
    @PostMapping("/principalUpload/{evaluationProcessId}")
    public ResponseMessage<String> clazzTemplateUpload(
            @NotNull(message = "请选择需要上传的文件") @ApiParam("选择需要上传的专业自评报告文件") @RequestPart MultipartFile file,
            @NotNull(message = "课程评审流程Id为空") @ApiParam("专业评审流程Id") @PathVariable("evaluationProcessId") String majorEvaluationProcessId) {
        return majorEvaluationProcessService.principalUploadMaterial(file, Long.valueOf(majorEvaluationProcessId)) ? ResponseMessage.newSuccessInstance("上传成功") : ResponseMessage.newErrorInstance("上传失败");
    }

    @ApiOperation("第一阶段 (评审流程负责人)绑定文件服务已上传的自评报告")
    @PostMapping("/principalBind/{evaluationProcessId}")
    public ResponseMessage<String> bindMajorMaterial(@Validated @RequestBody FileObjectBindBO fileObjectBindBO,
                                                     @NotNull(message = "课程评审流程Id为空") @ApiParam("专业评审流程Id") @PathVariable("evaluationProcessId") String majorEvaluationProcessId) {
        return majorEvaluationProcessService.principalBindMaterial(fileObjectBindBO.getFileObjectId(), Long.valueOf(majorEvaluationProcessId))
                ? ResponseMessage.newSuccessInstance("绑定成功")
                : ResponseMessage.newErrorInstance("绑定失败");
    }

    @ApiOperation("(专业评审专家)下载专业评审提交的文件")
    @GetMapping("/report/download")
    public void majorDownload(@Validated @ApiParam("文件加密路径") String path, HttpServletResponse response) {
        User loginUser = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
        if (fileObjectReferenceService.isFileObjectReference(path)) {
            try {
                fileObjectReferenceService.writeReferencedFileToResponse(path, response);
                log.info("用户 {} 下载专业文件对象成功: {}", loginUser.getRealName(), path);
            } catch (Exception e) {
                log.error("用户{}下载专业文件对象失败: {}", loginUser.getRealName(), e.getMessage());
            }
            return;
        }
        //解密路径
        String filePath = reportPath + File.separator + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);
        log.info("解密后文件路径: {}", filePath);
        // 截取文件名
        String fileName = StrUtil.subAfter(filePath, File.separator, true);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            // 配置文件下载及避免呢中午呢乱码
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 输入流
            bis = new BufferedInputStream(new FileInputStream(filePath));
            // 输出流
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int temp = 0;
            // 每次读取的字符串长度
            while ((temp = bis.read(buffer)) != -1) {
                os.write(buffer, 0, temp);
            }
            //os正常写入了
            log.info("用户 {} 下载专业评审文件成功: {}", loginUser.getRealName(), fileName);
        } catch (Exception e) {
            log.error("用户{}下载专业评审文件失败: {}", loginUser.getRealName(), e.getMessage());
        } finally {
            if (ObjectUtil.isNotNull(bis)) {
                try {
                    bis.close();
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }
            }
            if (ObjectUtil.isNotNull(fis)) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }
            }
        }
    }

    @ApiOperation("(专业负责人)查看自评报告模版信息")
    @GetMapping("/manager/template/{majorEvaluationProcessId}")
    public ResponseMessage<List<ReportTemplateResult>> managerGetTemplate(@PathVariable Long majorEvaluationProcessId) {
        // 获取当前专业类型
        String type = majorEvaluationProcessService.getMajorTypeByProcessId(majorEvaluationProcessId);
        // 获取自评报告模版
        List<ReportTemplate> reportTemplateByMajor = reportService.getReportTemplateByMajorList(type);
        List<ReportTemplateResult> reportTemplateResults = new ArrayList<>();
        String fileName = null;
        for (ReportTemplate reportTemplate : reportTemplateByMajor) {

            if (ObjectUtil.isNotNull(reportTemplate)) {
                fileName = StrUtil.subAfter(reportTemplate.getPath(), File.separator, true);
            }
            ReportTemplateResult result = new ReportTemplateResult();
            BeanUtils.copyProperties(reportTemplate, result);
            result.setFileName(fileName);
            reportTemplateResults.add(result);
        }
        return ResponseMessage.newSuccessInstance(reportTemplateResults);
    }

    @ApiOperation("(专业负责人)下载自评报告(模版)")
    @GetMapping("/manager/template/download/{reportId}")
    public ResponseMessage<String> managerDownload(HttpServletResponse response,
                                                   //@ApiParam("选择硬盘下载目录") @RequestParam("downloadPath") String downloadPath
                                                   @PathVariable int reportId) {
        // 获取当前登陆用户(专业负责人)对象
        User loginUser = UserUtil.getCurrentUser();
        // 获取自评报告模版所属服务器路径
        ReportTemplate reportTemplate = reportService.getReportTemplateById(reportId);

        if (reportTemplate == null) {
            throw new CustomException("暂无本专业自评报告模版");
        }

        log.info("开始获取文件");
        String filePath = reportTemplate.getPath();
        log.info("文件路径为：{}", filePath);
        // 截取文件名
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        log.info("文件名为：{}", fileName);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            // 配置文件下载及避免呢中午呢乱码
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 输入流
            bis = new BufferedInputStream(new FileInputStream(filePath));
            // 输出流
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int temp = 0;
            // 每次读取的字符串长度
            while ((temp = bis.read(buffer)) != -1) {
                os.write(buffer, 0, temp);
            }
            log.info("用户{} {}下载自评报告模版成功: {}", loginUser.getId(), loginUser.getRealName(), fileName);
            return ResponseMessage.newSuccessInstance("下载自评报告模版成功");
        } catch (Exception e) {
            log.error("用户{}下载自评报告模版失败: {}", loginUser.getRealName(), e.getMessage());
            return ResponseMessage.newErrorInstance("下载自评报告模版失败");
        } finally {
            if (ObjectUtil.isNotNull(bis)) {
                try {
                    bis.close();
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }
            }
            if (ObjectUtil.isNotNull(fis)) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }
            }
        }
    }

    @ApiOperation("根据自评报告加密路径查看报告内容")
    @GetMapping("/preview/{path}")
    public void getReport(@ApiParam(value = "自评报告加密路径") @PathVariable("path") @NotBlank(message = "路径为空") String path,
                          HttpServletResponse response) {
        User user = UserUtil.getCurrentUser();

        if (fileObjectReferenceService.isFileObjectReference(path)) {
            try {
                fileObjectReferenceService.writeReferencedFileToResponse(path, response);
                log.info("用户{} {}预览文件对象成功: {}", user.getId(), user.getRealName(), path);
            } catch (Exception e) {
                log.error("用户{} {} 预览文件对象失败: {}", user.getId(), user.getRealName(), e.getMessage());
            }
            return;
        }

        MajorEvaluationFile majorEvaluationFile = majorEvaluationFileService.getMajorEvaluationFileByPath(path);
        if (ObjectUtil.isNull(majorEvaluationFile)) {
            throw new CustomException("学校未上传自评报告模版或专业负责人未完成填写自评报告，暂无法查看");
        }


//        Report report = reportService.getReportByPath(path);
//        if (ObjectUtil.isNull(report)) {
//            throw new CustomException("学校未上传自评报告模版或专业负责人未完成填写自评报告，暂无法查看");
//        }

        // 解密uuid+文件名，拼接路径
        String filePath = reportPath + File.separator + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);

        // 截取文件名
        String fileName = StrUtil.subAfter(filePath, File.separator, true);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            // 配置文件下载及避免呢中午呢乱码
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            // 这里有坑，直接filePath就行，不用new File(filePath)
            bis = new BufferedInputStream(new FileInputStream(new File(filePath)));
            // 输出流
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int temp = 0;
            // 每次读取的字符串长度
            while ((temp = bis.read(buffer)) != -1) {
                os.write(buffer, 0, temp);
            }
            log.info("用户{} {}下载自评报告模版成功: {}", user.getId(), user.getRealName(), fileName);
        } catch (IOException e) {
            log.error("用户{} {} 下载自评报告模版失败: {}", user.getId(), user.getRealName(), e.getMessage());
        } finally {
            if (ObjectUtil.isNotNull(bis)) {
                try {
                    bis.close();
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }
            }
            if (ObjectUtil.isNotNull(fis)) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }
            }
        }
    }

    @ApiOperation("删除专业负责人已上传的专业自评报告")
    @DeleteMapping("/material")
    public ResponseMessage<List<MajorEvaluationGetFileResult>> deleteReport(@ApiParam("文件路径") @NotNull(message = "id为空") String path) {
        if (fileObjectReferenceService.isFileObjectReference(path)) {
            fileObjectReferenceService.deleteReferencedFile(path, UserUtil.getCurrentUser().getId());
        }
        return ResponseMessage.newSuccessInstance(majorEvaluationFileService.deleteMajorFileRecord(path));
    }

    @ApiOperation("获取专业负责人已上传的专业自评报告信息")
    @GetMapping("/material/{majorEvaluationProcessId}")
    public ResponseMessage<List<MajorEvaluationGetFileResult>> getReport(@ApiParam("专业评审的流程Id") @PathVariable("majorEvaluationProcessId") @NotNull(message = "id为空") String majorEvaluationProcessId) {
        return ResponseMessage.newSuccessInstance(majorEvaluationProcessService.getEvaluationMaterialInformation(Long.valueOf(majorEvaluationProcessId)));
    }

    @ApiOperation("(评审专家/组长)获取专业评估一级指标及其具体内容")
    @GetMapping("/option")
    public ResponseMessage<List<OptionResult>> listOptionByType(@ApiParam("要评审的专业名字") @NotBlank(message = "专业名字为空") String majorName) {
        return ResponseMessage.newSuccessInstance(reportService.listOptionByType(majorName));
    }

    @ApiOperation("(教学院长)评审专业报告")
    @PostMapping("/deanEvaluate")
    public ResponseMessage<String> deanEvaluate(@Validated @RequestBody DeanEvaluationBO deanEvaluationBO) {
        return majorEvaluationProcessService.saveDeanEvaluation(deanEvaluationBO) ? ResponseMessage.newSuccessInstance("已通过此文件") : ResponseMessage.newSuccessInstance("退回成功");
    }

    @ApiOperation("(评审专家)评审专业报告")
    @PostMapping("/expertReview")
    public ResponseMessage<String> masterEvaluate(@Validated @RequestBody MasterEvaluateBO masterEvaluateBO) {
        return majorEvaluationProcessService.saveMasterEvaluation(masterEvaluateBO) ? ResponseMessage.newSuccessInstance("提交评估结果成功") : ResponseMessage.newErrorInstance("提交评估结果失败");
    }

    @GetMapping("/evaluationFinishedReviews/{majorEvaluationProcessId}")
    @ApiOperation("专家组长获取 该评审全部已评审信息（指标、具体内容和选项）")
    public ResponseMessage<List<FinishedReviewVO>> getAllFinishReviews(@PathVariable @NotNull(message = "课程评审流程Id为空") Long majorEvaluationProcessId) {
        // 获取当前登陆用户(课程评审专家)对象
        return ResponseMessage.newSuccessInstance(majorEvaluationProcessService.getAllFinishedReviews(majorEvaluationProcessId));
    }

    @GetMapping("/expertFinishedReviewInfo")
    @ApiOperation("(评审专家)获取评审专家的评审内容（指标、具体内容和选项） 需要被查看专家的id和流程id")
    public ResponseMessage<MajorEvaluationResult> getFinishReview(@NotNull(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") @RequestParam("majorEvaluationProcessId") String majorEvaluationProcessId,
                                                                  @NotNull(message = "用户Id为空") @RequestParam("userId") Integer userId) {
        return ResponseMessage.newSuccessInstance(majorEvaluationRecodeService.getSingleMajorFinishReview(Long.valueOf(majorEvaluationProcessId), userId));
    }

    @ApiOperation("(专家组长)评审专业报告并确定是否及格")
    @PostMapping("/expertLeaderReview")
    public ResponseMessage<String> leaderEvaluate(@RequestBody @Validated LeaderEvaluateBO leaderEvaluateBO) {
        return majorEvaluationProcessService.saveLeaderEvaluation(leaderEvaluateBO) ? ResponseMessage.newSuccessInstance("提交评估结果成功") : ResponseMessage.newErrorInstance("提交评估结果失败");
    }

    @ApiOperation("(专家组长)获取自己的评审信息（指标、具体内容和选项）")
    @GetMapping("/leaderFinishedReviewReviews/{majorEvaluationProcessId}")
    public ResponseMessage<List<FinishedReviewVO>> getLeaderFinishReviews(@PathVariable @NotNull(message = "课程评审流程Id为空") Long majorEvaluationProcessId) {
        return ResponseMessage.newSuccessInstance(majorEvaluationProcessService.getLeaderFinishedReviews(majorEvaluationProcessId));
    }


    @GetMapping("/leaderFinishedReviewInfo")
    @ApiOperation("(专家组长)获取专家组长的评审内容（指标、具体内容和选项） 需要被查看专家组长的id和流程id")
    public ResponseMessage<MajorEvaluationResult> getLeaderFinishReview(@NotNull(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") @RequestParam("majorEvaluationProcessId") String majorEvaluationProcessId,
                                                                        @NotNull(message = "用户Id为空") @RequestParam("userId") Integer userId) {
        return ResponseMessage.newSuccessInstance(majorEvaluationRecodeService.getLeaderMajorFinishReview(Long.valueOf(majorEvaluationProcessId), userId));
    }

    @PostMapping("/updateMasterReview")
    @ApiOperation("(专家)更新自己的评审内容")
    public ResponseMessage<String> updateMasterEvaluation(@Validated @RequestBody MasterEvaluateBO masterEvaluateBO) {
        return majorEvaluationProcessService.updateMasterEvaluation(masterEvaluateBO) ? ResponseMessage.newSuccessInstance("更新评估结果成功") : ResponseMessage.newErrorInstance("更新评估结果失败");
    }


    @ApiOperation("专家组长 结束专业评审(使用)")
    @GetMapping("/endEvaluationProcess")
    public ResponseMessage<String> endEvaluationProcess(@Validated EndEvaluationProcessBO endEvaluationProcessBO) {
        return majorEvaluationProcessService.endEvaluationProcess(endEvaluationProcessBO) ? ResponseMessage.newSuccessInstance("结束评审流程成功") : ResponseMessage.newErrorInstance("结束评审流程失败");
    }

    @ApiOperation("专家组长 退回专家的评审")
    @PostMapping("/sendBackExpertReview/{majorEvaluationProcessId}/{expertId}")
    public ResponseMessage<String> sendBackEvaluation(@NotNull(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") @PathVariable("majorEvaluationProcessId") String evaluationProcessId,
                                                      @NotNull(message = "用户Id为空") @ApiParam("被退回的评审专家Id") @PathVariable("expertId") Integer userId) {
        return majorEvaluationProcessService.sendBackEvaluation(Long.valueOf(evaluationProcessId), userId) ? ResponseMessage.newSuccessInstance("退回评审成功") : ResponseMessage.newErrorInstance("退回评审失败");
    }

    @ApiOperation("(专家组长)查看所有评审组员信息      (这个功能先放着，后面再说)")
    @GetMapping("/evaluation/leader")
    public ResponseMessage<List<MasterInfoResult>> listMaster() {
        User loginUser = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
        if (!loginUser.getUserType().equals(UserType.LEADER.getType())) {
            return ResponseMessage.newErrorInstance("非评审专家组长，无法查看组员评估情况");
        }
        List<MasterInfoResult> masterList = reportService.listMaster(loginUser);
        if (CollUtil.isEmpty(masterList)) {
            return ResponseMessage.newErrorInstance("暂无组员");
        }
        return ResponseMessage.newSuccessInstance(masterList);
    }

    @ApiOperation("(专家组长)根据用户id退回评审专家评估结果")
    @PutMapping("/evaluation/master/{processId}/{userId}")
    public ResponseMessage<String> cancelEvaluation(@NotNull(message = "用户Id为空") @ApiParam("被退回评审的专家Id") @PathVariable("userId") Integer userId,
                                                    @PathVariable("processId") @NotNull(message = "专业评审流程Id为空") String evaluationProcessId) {
        return majorEvaluationProcessService.sendBackEvaluation(Long.valueOf(evaluationProcessId), userId) ? ResponseMessage.newSuccessInstance("退回评审成功") : ResponseMessage.newErrorInstance("退回评审失败");

    }

    @ApiOperation("结束评审流程(不使用)")
    @GetMapping("/endEvaluationProcess/{processId}")
    public ResponseMessage<String> endMajorEvaluationProcess(@NotNull(message = "专业评审流程Id为空") @PathVariable("processId") String evaluationProcessId) {
        return majorEvaluationProcessService.endEvaluationProcess(new EndEvaluationProcessBO(evaluationProcessId, "未评价")) ? ResponseMessage.newSuccessInstance("结束评审流程成功") : ResponseMessage.newErrorInstance("结束评审流程失败");
    }


    /**
     * 通用下载请求
     */
    private Map<String, InputStream> download(HttpServletResponse response,
                                              String fileName, String filePath,
                                              FileInputStream fis, BufferedInputStream bis) throws Exception {
        // 配置文件下载及避免呢中午呢乱码
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        // 输入流
        bis = new BufferedInputStream(new FileInputStream(filePath));
        // 输出流
        OutputStream os = response.getOutputStream();
        byte[] buffer = new byte[1024];
        int temp = 0;
        // 每次读取的字符串长度
        while ((temp = bis.read(buffer)) != -1) {
            os.write(buffer, 0, temp);
        }
        Map<String, InputStream> inputStreamMap = new HashMap<>();
        inputStreamMap.put("fis", fis);
        inputStreamMap.put("bis", bis);
        return inputStreamMap;
    }
}
