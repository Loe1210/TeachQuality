package com.vtmer.microteachingquality.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.domain.MajorReviewEvaluationFile;
import com.vtmer.microteachingquality.domain.MajorReviewEvaluationRecord;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationFile;
import com.vtmer.microteachingquality.service.MajorReviewEvaluationFileService;
import com.vtmer.microteachingquality.service.MajorReviewEvaluationRecordService;
import com.vtmer.microteachingquality.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Colin_Knight
 * @create 2023/12/13 20:36
 */
@RequestMapping("/majorEvaluationReview")
@RestController
@Api(tags = "专业评审复评模块相关接口")
@Slf4j
@PreAuthorize("hasAnyAuthority('all','major_evaluation_principal','major_evaluation_expert','major_evaluation_expert_leader','major_principal')")
public class MajorReviewEvaluationController {


    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);

    @Value("${report.path}")
    private String reportPath;

    private MajorReviewEvaluationRecordService majorReviewEvaluationRecordService;


    private MajorReviewEvaluationFileService majorReviewEvaluationFileService;

    @Autowired
    public MajorReviewEvaluationController(MajorReviewEvaluationRecordService majorReviewEvaluationRecordService,
                                           MajorReviewEvaluationFileService majorReviewEvaluationFileService) {
        this.majorReviewEvaluationFileService = majorReviewEvaluationFileService;
        this.majorReviewEvaluationRecordService = majorReviewEvaluationRecordService;
    }

    @ApiOperation("【复评阶段】获取评审阶段上传的文件信息")
    @GetMapping("/uploadedEvaluatedFiles/{majorId}")
    public ResponseMessage<List<MajorEvaluationFile>> getClazzEvaluationFiles(@NotNull(message = "专业Id为空") @PathVariable long majorId) {
        List<MajorEvaluationFile> uploadedFiles = majorReviewEvaluationFileService.getEvaluatedFiles(majorId);
        return uploadedFiles != null ? ResponseMessage.newSuccessInstance(uploadedFiles) : ResponseMessage.newErrorInstance("没有相关文件信息");
    }

    @ApiOperation("【复评阶段】复评专家评审改进材料并给出总评")
    @GetMapping("/adviceEvaluation")
    public ResponseMessage<String> endClazzReviewEvaluation(@NotNull(message = "内容不能为空") @RequestBody MajorReviewEvaluationRecord majorReviewOpinion) {
        Boolean result = majorReviewEvaluationRecordService.endMajorReviewOpinion(majorReviewOpinion);
        return result ? ResponseMessage.newSuccessInstance("复评结束成功") : ResponseMessage.newErrorInstance("复评结束失败");
    }

    @ApiOperation("【复评阶段】根据专家意见创建复评流程")
    @GetMapping("/createClazzReviewEvaluationList/{majorEvaluationProcessId}/{necessary}")
    public ResponseMessage<String> createReview(@PathVariable long majorEvaluationProcessId, @PathVariable int necessary) {
        String msg = majorReviewEvaluationRecordService.insert(majorEvaluationProcessId, necessary);
        return ResponseMessage.newErrorInstance(msg);
    }

    @ApiOperation("【复评阶段】获取上传的复评文件信息")
    @GetMapping("/uploadedFiles/{majorEvaluationProcessId}")
    public ResponseMessage<List<MajorReviewEvaluationFile>> getReviewEvaluationUploadFiles(@NotNull(message = "专业评审复评流程流程Id为空") @PathVariable String majorEvaluationProcessId) {
        Integer userId = UserUtil.getCurrentUser().getId();
        return ResponseMessage.newSuccessInstance(majorReviewEvaluationFileService.getUploadedFilesInfo(userId, Long.valueOf(majorEvaluationProcessId)));
    }

    @ApiOperation("【复评阶段】(评审流程负责人)上传已填写的复评报告")
    @PostMapping("/principalUpload/{majorEvaluationId}")
    public ResponseMessage<String> majorTemplateUpload(
            @NotNull(message = "请选择需要上传的文件") @ApiParam("选择需要上传的课程自评报告文件") @RequestPart MultipartFile file,
            @NotBlank(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") @PathVariable("majorEvaluationId") String majorEvaluationProcessId) throws IOException, MQBrokerException, RemotingException, InterruptedException, MQClientException {
        return majorReviewEvaluationFileService.principalUploadMaterial(file, Long.valueOf(majorEvaluationProcessId)) ? ResponseMessage.newSuccessInstance("上传成功") : ResponseMessage.newErrorInstance("上传失败");
    }

    @ApiOperation("【复评阶段】下载课程提交的复评报告")
    @GetMapping("/report/download")
    public void majorDownload(@Validated @ApiParam("文件加密路径") String path, HttpServletResponse response) {
        User loginUser = JSON.parseObject(JSONUtil.toJsonStr(SecurityContextHolder.getContext().getAuthentication().getPrincipal()), User.class);
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
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

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


}
