package com.vtmer.microteachingquality.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.model.bo.ClazzEvaluationLeaderBO;
import com.vtmer.microteachingquality.model.bo.SubmitEvaluationRecordBO;
import com.vtmer.microteachingquality.model.dto.ClazzInJudgeResultDTO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzFile;
import com.vtmer.microteachingquality.model.vo.*;
import com.vtmer.microteachingquality.service.ClazzEvaluationProcessService;
import com.vtmer.microteachingquality.service.ClazzFileService;
import com.vtmer.microteachingquality.service.ClazzService;
import com.vtmer.microteachingquality.service.CourseEvaluationExpertService;
import com.vtmer.microteachingquality.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Hung
 * @date 2022/4/20 19:42
 */
@RestController
@Api(tags = "课程评审相关接口")
@RequestMapping("/clazzEvaluation")
@Slf4j
@Validated
@PreAuthorize("hasAnyAuthority('clazz_principal','all','clazz_expert_leader','clazz_expert')")
public class ClazzEvaluationProcessController implements EvaluationProcessStatus {

    /**
     * 随机生成存储加密文件名(课程自评报告)的密钥
     * 固定密钥，不然会出bug
     */
    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();
    /**
     * 构建加密
     */
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);
    @Value("${clazz.path}")
    private String clazzPath;
    @Resource
    private ClazzService clazzService;
    @Resource
    private ClazzEvaluationProcessService clazzEvaluationProcessService;
    @Resource
    private ClazzFileService clazzFileService;
    @Resource
    private CourseEvaluationExpertService courseEvaluationExpertService;


    @PostMapping("/evaluationProcess/{clazzId}")
    @ApiOperation("创建评审流程")
    public ResponseMessage<String> createNewEvaluationProcess(@NotNull(message = "课程id为空") @ApiParam("课程ID") @PathVariable("clazzId") Integer clazzId) {
        return clazzEvaluationProcessService.createEvaluationProcess(clazzId) ? ResponseMessage.newSuccessInstance("流程创建成功") : ResponseMessage.newErrorInstance("流程创建失败");
    }

    @DeleteMapping("/evaluationProcess/{clazzEvaluationProcessId}")
    @ApiOperation("删除评审流程")
    public ResponseMessage<String> deleteEvaluationProcess(@ApiParam(value = "流程id", required = true) @PathVariable Long clazzEvaluationProcessId) {
        return ResponseMessage.newSuccessInstance(clazzEvaluationProcessService.deleteEvaluationProcess(clazzEvaluationProcessId));
    }

    @GetMapping("/allEvaluationProcess/{clazzId}")
    @ApiOperation("获取这个课程的所有评审流程")
    public ResponseMessage<List<ClazzEvaluationProcessSimpleInfoVO>> getAllEvaluationProcesses(@NotNull(message = "课程id为空") @ApiParam("课程ID") @PathVariable("clazzId") Integer clazzId,
                                                                                               //@ApiParam("查询条件") @RequestBody Map<String, String> conditionMap,
                                                                                               @ApiParam("每页显示的条数") @NotNull(message = "页大小为空") @RequestParam Integer pageSize,
                                                                                               @ApiParam("页码") @NotNull(message = "页码为空") @RequestParam Integer pageNum) {
        return ResponseMessage.newSuccessInstance(clazzEvaluationProcessService.getEvaluationProcesses(clazzId, pageSize, pageNum, new HashMap<>()));
    }

    @ApiOperation("获取课程评审流程的状态信息")
    @GetMapping("/status")
    public ResponseMessage<ClazzEvaluationProcessInfo> clazzEvaluationProcessInfo(@NotNull(message = "课程评审流程Id为空") @RequestParam Long clazzEvaluationId) {
        return ResponseMessage.newSuccessInstance(clazzEvaluationProcessService.getEvaluationProcessInfo(clazzEvaluationId));
    }

    @ApiOperation("第一阶段 (评审流程负责人)上传已填写的自评报告")
    @PostMapping("/principalUpload/{clazzEvaluationId}")
    public ResponseMessage<String> clazzTemplateUpload(
            @NotNull(message = "请选择需要上传的文件") @ApiParam("选择需要上传的课程自评报告文件") @RequestPart MultipartFile file,
            @NotBlank(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") @PathVariable("clazzEvaluationId") String clazzEvaluationProcessId) throws IOException, MQBrokerException, RemotingException, InterruptedException, MQClientException {
        return clazzEvaluationProcessService.principalUploadMaterial(file, Long.valueOf(clazzEvaluationProcessId)) ? ResponseMessage.newSuccessInstance("上传成功") : ResponseMessage.newErrorInstance("上传失败");
    }

    @ApiOperation("获取所有课程评价的相关信息：文件path，评审状态，课程信息等 (作废)")
    @GetMapping("/clazzInfo")
    public ResponseMessage<List<GetAllClazzInfoResult>> getAllClazzInformation() {
        return ResponseMessage.newSuccessInstance(clazzService.getAllClazzInformation());
    }

    @ApiOperation("根据专家的类型获取其所属的所有课程(暂时不用)")
    @GetMapping("/allClazz")
    public ResponseMessage<List<ClazzVO>> getAllClazzByType(@NotNull(message = "页数为空") @ApiParam("页数") Integer pageNum,
                                                            @NotNull(message = "页大小为空") @ApiParam("每页大小") Integer pageSize) {
        User currentUser = UserUtil.getCurrentUser();
        return ResponseMessage.newSuccessInstance(clazzService.getClazzByUserType(currentUser, pageNum, pageSize));
    }

    @ApiOperation("通过专家id获取该专家要评审的所有课程信息(暂时不用)")
    @GetMapping("/clazzEvaluationInfo")
    public ResponseMessage<List<GetEvaluationClazzByUserIdResult>> getEvaluationClazzByUserId() {
        return ResponseMessage.newSuccessInstance(clazzService.getEvaluationClazzByUserId(UserUtil.getCurrentUser().getId()));
    }

    @ApiOperation("获取评审流程的课程负责人获取上传的文件信息")
    @GetMapping("/uploadedFiles")
    public ResponseMessage<List<GetUploadedFilesResult>> getEvaluationProcessUploadFiles(@NotNull(message = "课程评审流程Id为空") @RequestParam String clazzEvaluationProcessId) {
        Integer userId = UserUtil.getCurrentUser().getId();
        return ResponseMessage.newSuccessInstance(clazzEvaluationProcessService.getUploadedFiles(userId, Long.valueOf(clazzEvaluationProcessId)));
    }

    @ApiOperation("课程负责人删除自己上传的文件")
    @DeleteMapping("/uploadedFiles")
    public ResponseMessage<?> deleteUploadedFile(@NotNull(message = "路径为空") @ApiParam("文件路径") String path) {
        User loginUser = UserUtil.getCurrentUser();
        //解密路径
        String filePath = clazzPath + File.separator + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);
        ClazzFile clazzFile = clazzFileService.getClazzFile(path);

        if (!clazzFile.getUserId().equals(loginUser.getId())) {
            throw new CustomException("非本人上传文件，您没有权限删除该文件");
        }

        log.info("解密后文件路径: {}", filePath);
        // 截取文件名
        String fileName = StrUtil.subAfter(filePath, File.separator, true);
        try {
            // 输入流
            File file = new File(filePath);
            if (clazzFileService.deleteClazzFileRecord(clazzFile.getId()) && file.delete() && file.exists()) {
                log.error("用户{} 删除课程自评报告失败: {}", loginUser.getRealName(), file.getName());
                throw new CustomException("删除课程自评报告失败");
            } else {
                //os正常写入了
                log.info("用户 {} 删除课程自评报告成功: {}", loginUser.getRealName(), fileName);
                return ResponseMessage.newSuccessInstance("删除课程自评报告成功");
            }
        } catch (Exception e) {
            log.error("用户{}删除课程自评报告失败: {}", loginUser.getRealName(), e.getMessage());
        }
        return ResponseMessage.newErrorInstance("删除课程自评报告失败");
    }

    @ApiOperation("(课程评审专家)下载课程提交的自评报告")
    @GetMapping("/report/download")
    public void clazzDownload(@Validated @ApiParam("文件加密路径") String path, HttpServletResponse response) {
        User loginUser = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
        //解密路径
        String filePath = clazzPath + File.separator + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);
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
            log.info("用户 {} 下载课程自评报告成功: {}", loginUser.getRealName(), fileName);
        } catch (Exception e) {
            log.error("用户{}下载课程自评报告失败: {}", loginUser.getRealName(), e.getMessage());
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


    @ApiOperation("课程评审专家/负责人 导出自己的记录，在结束流程后才显现")
    @GetMapping("/exportRecord")
    public void exportRecord(HttpServletResponse response) {
        User user = UserUtil.getCurrentUser();
        XSSFWorkbook xssfWorkbook = clazzService.exportRecord(user.getId());
        try {
            // 配置文件下载及避免乱码
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(new SimpleDateFormat("MM月dd日 HH时mm分ss秒").format(new Date()) + ".xlsx", "UTF-8"));

            OutputStream os = response.getOutputStream();
            xssfWorkbook.write(os);
        } catch (IOException e) {
            log.info("文件输出失败");
        }
        log.info("文件输出成功");
    }

    @ApiOperation("课程评审专家退回评审")
    @PostMapping("/evaluationBack")
    public ResponseMessage<String> sendBackEvaluation(@NotNull(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") String clazzEvaluationProcessId) {
        return clazzEvaluationProcessService.sendBackEvaluation(Long.valueOf(clazzEvaluationProcessId)) ? ResponseMessage.newSuccessInstance("退回评审成功") : ResponseMessage.newErrorInstance("退回评审失败");
    }

    @PostMapping("/stayclazztoreview")
    @ApiOperation("分页获取 待评审报告信息 (作废)")
    public ResponseMessage<?> stayListResponseMessage(Integer pageNum, Integer pageSize) {
        // 获取当前登陆用户(课程评审专家)对象
        return ResponseMessage.newSuccessInstance(courseEvaluationExpertService.getCourseMessageOfEvaluationExpert(0, pageNum, pageSize));
    }

    @PostMapping("/finishclazztoreview")
    @ApiOperation("分页获取 课程评审专家已评审报告信息 (作废)")
    public ResponseMessage<?> finishListResponseMessage(Integer pageNum, Integer pageSize) {
        return ResponseMessage.newSuccessInstance(courseEvaluationExpertService.getCourseMessageOfEvaluationExpert(1, pageNum, pageSize));
    }

    @GetMapping("/evaluationReviewOptions")
    @ApiOperation("课程专家 获取评审信息（指标和具体内容）")
    public ResponseMessage<List<ClazzInJudgeResultDTO>> getReviewOptions(@NotNull(message = "课程Id为空") @ApiParam("课程Id") @RequestParam String clazzId) {
        return ResponseMessage.newSuccessInstance(courseEvaluationExpertService.getClazzInJudgeByClazzType(Integer.valueOf(clazzId)));
    }

    @GetMapping("/myselfFinishedReviewInfo")
    @ApiOperation("课程专家 获取自己的已经评审的信息（指标、具体内容和选项）")
    public ResponseMessage<ClazzFinishResult> getMyselfFinishReview(@NotNull(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") @RequestParam String clazzEvaluationProcessId) {
        // 获取当前登陆用户(课程评审专家)对象
        User user = UserUtil.getCurrentUser();
        return getFinishReview(clazzEvaluationProcessId, user.getId());
    }

    @GetMapping("/expertFinishedReviewInfo")
    @ApiOperation("根据课程评审流程id和userId获取 其他评审专家的评审内容（指标、具体内容和选项） 需要被查看专家的id")
    public ResponseMessage<ClazzFinishResult> getFinishReview(@NotNull(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") @RequestParam("clazzEvaluationProcessId") String clazzEvaluationProcessId,
                                                              @NotNull(message = "用户Id为空") @RequestParam("userId") Integer userId) {
        return ResponseMessage.newSuccessInstance(courseEvaluationExpertService.getSingleClazzFinishReview(Long.valueOf(clazzEvaluationProcessId), userId));
    }

    @GetMapping("/evaluationFinishedReviews/{clazzEvaluationProcessId}")
    @ApiOperation("课程专家组长获取 该评审全部已评审信息（指标、具体内容和选项）")
    public ResponseMessage<List<FinishedReviewVO>> getAllFinishReviews(@PathVariable @NotNull(message = "课程评审流程Id为空") Long clazzEvaluationProcessId) {
        // 获取当前登陆用户(课程评审专家)对象
        return ResponseMessage.newSuccessInstance(clazzEvaluationProcessService.getAllFinishedReviews(clazzEvaluationProcessId));
    }

    @PostMapping("/sendBackExpertReview")
    @ApiOperation("课程专家组长退回课程专家的评审")
    public ResponseMessage<?> sendBackExpertReview(@NotNull(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") String evaluationProcessId,
                                                   @NotNull(message = "用户Id为空") @ApiParam("被退回的评审专家Id") Integer userId) {
        return clazzEvaluationProcessService.sendBackExpertReview(Long.valueOf(evaluationProcessId), userId) ? ResponseMessage.newSuccessInstance("退回评审成功") : ResponseMessage.newErrorInstance("退回评审失败");
    }

    @PostMapping("/evaluationRecord")
    @ApiOperation("课程评审专家和课程评审专家组长上传答题记录")
    public ResponseMessage<String> submitEvaluationRecord(@RequestBody @Validated SubmitEvaluationRecordBO submitEvaluationRecordBO) {
        //TODO 更改课程评审专家组长
        return courseEvaluationExpertService.insertEvaluationRecord(submitEvaluationRecordBO) ? ResponseMessage.newSuccessInstance("评审成功") : ResponseMessage.newErrorInstance("评审失败，请重新尝试");
    }

    @GetMapping("/evaluationFiles")
    @ApiOperation("课程评审专家获取课程文件列表")
    public ResponseMessage<List<GetClazzFilesResult>> getEvaluationFiles(@NotNull(message = "课程评审流程Id不能为空") String clazzEvaluationProcessId) {
        return ResponseMessage.newSuccessInstance(courseEvaluationExpertService.getAllEvaluationFiles(Long.valueOf(clazzEvaluationProcessId)));
    }

    @PostMapping("/insertEmptyData")
    @ApiOperation("插入空白数据用接口（后台用的，前端别测试这个）")
    public ResponseMessage<?> insertEmptyData(String tableName, Integer size) {
        return ResponseMessage.newSuccessInstance(courseEvaluationExpertService.insertEmptyData(tableName, size));
    }

    @ApiOperation("保存专家组长的小组意见")
    @PostMapping("/leaderReview")
    public ResponseMessage<String> postClazzEvaluationLeaderReview(@Validated @RequestBody ClazzEvaluationLeaderBO evaluationLeaderBO) {
        return clazzEvaluationProcessService.postClazzEvaluationLeaderReview(evaluationLeaderBO) ? ResponseMessage.newSuccessInstance("评审成功") : ResponseMessage.newErrorInstance("评审失败");
    }

    @GetMapping("/leaderReviews")
    @ApiOperation("查看 这个评审流程有多少个专家组长的小组意见")
    public ResponseMessage<List<ClazzEvaluationLeaderReviewVO>> getClazzEvaluationLeaderReviews(@NotNull(message = "课程评审流程Id不能为空") String clazzEvaluationProcessId) {
        return ResponseMessage.newSuccessInstance(clazzEvaluationProcessService.getClazzEvaluationLeaderReviews(Long.valueOf(clazzEvaluationProcessId)));
    }

    @GetMapping("/endGroupEvaluation/{clazzEvaluationId}")
    @ApiOperation("结束评审的小组评审流程")
    public ResponseMessage<String> setEvaluationEnd(@PathVariable Long clazzEvaluationId) {
        return clazzEvaluationProcessService.endGroupEvaluation(clazzEvaluationId) ? ResponseMessage.newSuccessInstance("已结束小组评审") : ResponseMessage.newErrorInstance("出现未知错误");
    }

    @PostMapping("/evaluationEnd")
    @ApiOperation("结束评审流程")
    public ResponseMessage<String> setEvaluationEnd(@NotNull(message = "课程评审流程Id不能为空") @RequestParam String clazzEvaluationProcessId,
                                                    @ApiParam("结束评审流程的评语 特优、优秀、良好、尚可、待改进") @RequestParam String remark) {
        return clazzEvaluationProcessService.endClazzEvaluationProcess(Long.valueOf(clazzEvaluationProcessId), remark) ? ResponseMessage.newSuccessInstance("评审结束成功") : ResponseMessage.newErrorInstance("评审结束失败");
    }
}
