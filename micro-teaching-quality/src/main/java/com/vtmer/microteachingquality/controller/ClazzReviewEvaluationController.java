package com.vtmer.microteachingquality.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.domain.ClazzReviewEvaluationFile;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzOpinionRecord;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzReviewEvaluationRecord;
import com.vtmer.microteachingquality.model.vo.ClazzReviewInfo;
import com.vtmer.microteachingquality.model.vo.GetUploadedFilesResult;
import com.vtmer.microteachingquality.service.ClazzReviewEvaluationFileService;
import com.vtmer.microteachingquality.service.ClazzReviewEvaluationRecordService;
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
 * @create 2023/12/11 23:19
 */
@RestController
@Api(tags = "课程评审复评相关接口")
@RequestMapping("/clazzEvaluationReview")
@Slf4j
@Validated
@PreAuthorize("hasAnyAuthority('clazz_principal','all','clazz_expert_leader','clazz_expert')")
public class ClazzReviewEvaluationController {

    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();
    /**
     * 构建加密
     */
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);

    @Value("${clazz.path}")
    private String clazzPath;

    @Autowired
    private ClazzReviewEvaluationRecordService clazzReviewEvaluationRecordService;

    @Autowired
    private ClazzReviewEvaluationFileService clazzReviewEvaluationFileService;

    @ApiOperation("【复评阶段】获取评审改进建议")
    @GetMapping("/getClazzEvaluationOpinionList/{evaluationId}")
    public ResponseMessage<List<ClazzOpinionRecord>> getClazzOpinions(@PathVariable Long evaluationId) {
        List<ClazzOpinionRecord> clazzOpinions = clazzReviewEvaluationRecordService.getClazzOpinions(evaluationId);
        return clazzOpinions != null ? ResponseMessage.newSuccessInstance(clazzOpinions) : ResponseMessage.newErrorInstance("查询不到相关建议");
    }

    @ApiOperation("【复评阶段】获取评审阶段上传的文件信息")
    @GetMapping("/uploadedEvaluatedFiles")
    public ResponseMessage<List<GetUploadedFilesResult>> getClazzEvaluationFiles(@NotNull(message = "课程评审流程Id为空") @RequestParam Long clazzEvaluationProcessId) {
        List<GetUploadedFilesResult> uploadedFiles = clazzReviewEvaluationFileService.getUploadedAdviceFilesInfo(clazzEvaluationProcessId);
        return uploadedFiles != null ? ResponseMessage.newSuccessInstance(uploadedFiles) : ResponseMessage.newErrorInstance("没有相关文件信息");
    }

    @ApiOperation("【复评阶段】复评专家评审改进材料并给出总评")
    @PostMapping("/adviceEvaluation")
    public ResponseMessage<String> endClazzReviewEvaluation(@NotNull(message = "内容不能为空") @RequestBody ClazzReviewEvaluationRecord clazzReviewEvaluationRecord) {
        Boolean result = clazzReviewEvaluationRecordService.endClazzReviewOpinion(clazzReviewEvaluationRecord);
        return result ? ResponseMessage.newSuccessInstance("复评结束成功") : ResponseMessage.newErrorInstance("复评结束失败");
    }

    @ApiOperation("【复评阶段】根据专家意见创建复评流程")
    @GetMapping("/createClazzReviewEvaluationList/{evaluationId}/{necessary}")
    public ResponseMessage<String> createReview(@PathVariable long evaluationId, @PathVariable int necessary) {
        String msg = clazzReviewEvaluationRecordService.insert(evaluationId, necessary);
        return ResponseMessage.newErrorInstance(msg);
    }


    @ApiOperation("【复评阶段】获取复评流程信息")
    @GetMapping("/getClazzReviewEvaluationInfo")
    public ResponseMessage<ClazzReviewInfo> getReviewInfo(long evaluationId) {
        ClazzReviewInfo clazzReviewInfo = clazzReviewEvaluationRecordService.getClazzReviewInfo(evaluationId);
        return clazzReviewInfo == null ? ResponseMessage.newErrorInstance("未找到对应复评流程") : ResponseMessage.newSuccessInstance(clazzReviewInfo);
    }

    @ApiOperation("【复评阶段】获取上传的复评文件信息")
    @GetMapping("/uploadedFiles")
    public ResponseMessage<List<ClazzReviewEvaluationFile>> getReviewEvaluationUploadFiles(@NotNull(message = "课程评审流程Id为空") @RequestParam String clazzEvaluationProcessId) {
        Integer userId = UserUtil.getCurrentUser().getId();
        return ResponseMessage.newSuccessInstance(clazzReviewEvaluationFileService.getUploadedFilesInfo(userId, Long.valueOf(clazzEvaluationProcessId)));
    }

    @ApiOperation("【复评阶段】(评审流程负责人)上传已填写的复评报告")
    @PostMapping("/principalUpload/{clazzEvaluationId}")
    public ResponseMessage<String> clazzTemplateUpload(
            @NotNull(message = "请选择需要上传的文件") @ApiParam("选择需要上传的课程自评报告文件") @RequestPart MultipartFile file,
            @NotBlank(message = "课程评审流程Id为空") @ApiParam("课程评审流程Id") @PathVariable("clazzEvaluationId") String clazzEvaluationProcessId) throws IOException, MQBrokerException, RemotingException, InterruptedException, MQClientException {
        return clazzReviewEvaluationFileService.principalUploadMaterial(file, Long.valueOf(clazzEvaluationProcessId)) ? ResponseMessage.newSuccessInstance("上传成功") : ResponseMessage.newErrorInstance("上传失败");
    }

    @ApiOperation("【复评阶段】下载课程提交的复评报告")
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
