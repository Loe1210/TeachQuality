package com.vtmer.microteachingquality.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus;
import com.vtmer.microteachingquality.model.bo.CreateNewMajorBatchVO;
import com.vtmer.microteachingquality.model.bo.MajorArchiveReviewBO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.vo.*;
import com.vtmer.microteachingquality.service.MajorArchiveProcessService;
import com.vtmer.microteachingquality.service.MajorArchiveService;
import com.vtmer.microteachingquality.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author HJW
 */
@RestController
@Api(tags = "专业归档相关接口")
@RequestMapping("/majorArchive")
@Slf4j
@PreAuthorize("hasAnyAuthority('all','major_archive_expert','major_archive_principal')")
@Validated
public class MajorArchiveController implements EvaluationProcessStatus {
    /**
     * 随机生成存储加密文件名(课程自评报告)的密钥
     * 固定密钥，不然会出bug
     */
    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();

    /**
     * 构建加密
     */
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);
    @Value("${major.archive.file}")
    private String archiveFilePath;
    @Value("${major.archive.template}")
    private String templatePath;

    @Resource
    private MajorArchiveProcessService majorArchiveProcessService;
    @Resource
    private MajorArchiveService majorArchiveService;


    @PostMapping("/process")
    @ApiOperation("创建专业归档批次 ")
    public ResponseMessage<String> createProcess(@RequestBody @Validated CreateNewMajorBatchVO createNewMajorBatchVO) {
        return majorArchiveProcessService.createMajorArchiveBatch(createNewMajorBatchVO) ? ResponseMessage.newSuccessInstance("专业归档批次创建成功") : ResponseMessage.newErrorInstance("专业规定批次创建失败");
    }

    @ApiOperation(("获取当前归档批次的信息"))
    @GetMapping("/status/{majorArchiveBatchId}")
    public ResponseMessage<MajorArchiveProcessInfo> getBatchProcessInfo(
            @PathVariable("majorArchiveBatchId") @ApiParam("专业归档批次id") @NotNull(message = "专业归档批次Id为空") String majorArchiveBatchId) {
        return ResponseMessage.newSuccessInstance(majorArchiveProcessService.getMajorArchiveProcessInfo(Long.valueOf(majorArchiveBatchId)));
    }


    @ApiOperation(value = "专业归档 上传文件")
    @PostMapping("/uploadFile/{batchId}")
    public ResponseMessage<String> uploadFile(@RequestPart MultipartFile file,
                                              @NotNull(message = "专业归档批次Id为空") @PathVariable("batchId") String batchId) throws IOException {

        // 获取当前登陆用户(课程负责人)对象
        User loginUser = UserUtil.getCurrentUser();
        if (ObjectUtil.isNull(file) || StrUtil.isBlank(file.getOriginalFilename())) {
            return ResponseMessage.newErrorInstance("请选择需要上传的文件");
        }

        // 目录添加uuid防重
        String uuid = IdUtil.simpleUUID();
        File path = new File(archiveFilePath + File.separator + uuid);
        if (!path.isDirectory()) {
            path.mkdirs();
        }
        // 加密文件名
        String encryptFileName = aes.encryptHex(uuid + File.separator + file.getOriginalFilename());
        String filePath = archiveFilePath + File.separator + uuid + File.separator + file.getOriginalFilename();
        log.info("用户 :{}   上传归档文件 : {}    加密路径 :  {}", loginUser.getRealName(), file.getOriginalFilename(), encryptFileName);

        //进行信息存储
        if (majorArchiveProcessService.uploadFileRecord(loginUser, Long.valueOf(batchId), file.getOriginalFilename(), encryptFileName)) {
            FileUtil.writeBytes(file.getBytes(), filePath);
            log.info("用户 :{}   上传归档文件 : {}   上传成功", loginUser.getRealName(), file.getOriginalFilename());
            return ResponseMessage.newSuccessInstance("上传文件成功");
        } else {
            log.info("用户 :{}   上传归档文件 : {}   上传失败", loginUser.getRealName(), file.getOriginalFilename());
            return ResponseMessage.newErrorInstance("上传文件失败");
        }

    }

    @GetMapping("/downloadUploadedFile")
    @ApiOperation("专业归档 下载自己上传的文件")
    public void downloadFile(@NotBlank(message = "传入路径为空") @ApiParam("加密路径") String path,
                             HttpServletResponse response) {
        User loginUser = UserUtil.getCurrentUser();
        //解密路径
        String filePath = archiveFilePath + File.separator + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);
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
            log.info("用户 {} 下载归档文件成功: {}", loginUser.getRealName(), fileName);
        } catch (Exception e) {
            log.error("用户  {}  下载归档文件失败: {}", loginUser.getRealName(), e.getMessage());
        } finally {
            if (ObjectUtil.isNotNull(bis)) {
                try {
                    bis.close();
                } catch (IOException e) {
                    log.error("用户 {} 下载归档文件失败： {}", loginUser.getRealName(), e.getMessage());
                }
            }
            if (ObjectUtil.isNotNull(fis)) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("用户 {} 下载归档文件失败： {}", loginUser.getRealName(), e.getMessage());
                }
            }
        }
    }

    @ApiOperation("专业归档 删除已经上传的文件")
    @PostMapping("/deleteUploadedFile")
    public ResponseMessage<String> deleteUploadedFile(@RequestBody @NotBlank(message = "传入路径为空") String path) {
        User loginUser = UserUtil.getCurrentUser();
        //解密路径
        String filePath = archiveFilePath + File.separator + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);
        log.info("解密后文件路径: {}", filePath);
        // 截取文件名
        String fileName = StrUtil.subAfter(filePath, File.separator, true);
        try {
            if (majorArchiveProcessService.deleteUploadedFileRecord(loginUser, path) <= 0) {
                return ResponseMessage.newErrorInstance("文件已经不存在");
            }
            // 输入流
            File file = new File(filePath);
            file.delete();
            if (file.exists()) {
                log.error("用户  {}  删除归档文件失败: {}", loginUser.getRealName(), file.getName());
            } else {
                //os正常写入了
                log.info("用户 {} 删除归档文件成功: {}", loginUser.getRealName(), fileName);
            }
        } catch (Exception e) {
            log.error("用户：{}   删除归档文件失败: {}", loginUser.getRealName(), e.getMessage());
        }
        return ResponseMessage.newSuccessInstance("删除文件成功");
    }


    @ApiOperation("专业归档 获取自己上传的所有文件（包括批次名，不同批次里的文件信息等）")
    @GetMapping("/uploadedFilesInfo/{batchId}")
    public ResponseMessage<MajorArchiveGetUploadedFilesInfoResult> getUploadedFilesInfo(@PathVariable("batchId") @NotNull(message = "批次Id为空") String batchId) {
        return ResponseMessage.newSuccessInstance(majorArchiveProcessService.getUploadedFilesInfo(Long.valueOf(batchId)));
    }


    @ApiOperation("专业归档 获取模板文件信息")
    @GetMapping("/templateFilesInfo")
    public ResponseMessage<List<MajorArchiveGetTemplateFilesInfoResult>> getTemplateFilesInfo() {
        return ResponseMessage.newSuccessInstance(majorArchiveProcessService.getTemplateFilesInfo());
    }


    @ApiOperation("专业归档负责人(超级管理员) 上传模板文件")
    @PostMapping("/uploadTemplateFile")
    public ResponseMessage<?> uploadTemplateFile(@RequestPart MultipartFile file) throws IOException {
        // 获取当前登陆用户(课程负责人)对象
        User loginUser = UserUtil.getCurrentUser();
        if (ObjectUtil.isNull(file)) {
            return ResponseMessage.newErrorInstance("请选择需要上传的文件");
        } else if (StrUtil.isBlank(file.getOriginalFilename())) {
            return ResponseMessage.newErrorInstance("请选择需要上传的文件");
        }
        // 目录添加uuid防重
        String uuid = IdUtil.simpleUUID();
        File path = new File(templatePath + File.separator + uuid);
        if (!path.isDirectory()) {
            path.mkdirs();
        }
        // 加密文件名
        String encryptFileName = aes.encryptHex(uuid + File.separator + file.getOriginalFilename());
        String filePath = templatePath + File.separator + uuid + File.separator + file.getOriginalFilename();
        log.info("用户 :{}   上传专业归档模板文件 : {}    加密路径 :  {}", loginUser.getRealName(), file.getOriginalFilename(), encryptFileName);
        //进行信息存储
        if (majorArchiveProcessService.uploadTemplateFileRecord(loginUser, file.getOriginalFilename(), encryptFileName) >= 0) {
            FileUtil.writeBytes(file.getBytes(), filePath);
            log.info("用户 :{}   上传专业归档模板文件  : {}   上传成功", loginUser.getRealName(), file.getOriginalFilename());
            return ResponseMessage.newSuccessInstance("上传文件成功");
        } else {
            log.info("用户 :{}   上传专业归档模板文件  : {}   上传失败", loginUser.getRealName(), file.getOriginalFilename());
            return ResponseMessage.newErrorInstance("上传文件失败");
        }
    }


    @ApiOperation("专业归档负责人(超级管理员) 删除模板文件")
    @PostMapping("/deleteTemplateFile")
    public ResponseMessage<?> deleteTemplateFile(String path) {
        User loginUser = UserUtil.getCurrentUser();
        boolean result = false;
        String filePath = templatePath + File.separator + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);
        log.info("解密后文件路径: {}", filePath);
        // 截取文件名
        String fileName = StrUtil.subAfter(filePath, File.separator, true);
        try {
            // 输入流
            File file = new File(filePath);
            result = file.delete();
            if (file.exists()) {
                result = false;
                log.error("用户{}删除课程自评报告失败: {}", loginUser.getRealName(), file.getName());
            } else {
                //os正常写入了
                log.info("用户 {} 删除课程自评报告成功: {}", loginUser.getRealName(), fileName);
            }
            if (majorArchiveProcessService.deleteTemplateFileRecord(fileName, path) <= 0) {
                return ResponseMessage.newErrorInstance("文件已经不存在");
            }
        } catch (Exception e) {
            log.error("用户{}删除课程自评报告失败: {}", loginUser.getRealName(), e.getMessage());
        }
        return ResponseMessage.newSuccessInstance(result);
    }


    //根据模板path下载模板
    @ApiOperation("专业归档 根据模板文件path下载模板")
    @GetMapping("/downloadTemplateFile")
    public void downloadTemplateFile(String path, HttpServletResponse response) {
        User loginUser = UserUtil.getCurrentUser();
        //解密路径
        String filePath = templatePath + File.separator + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);
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
            log.info("用户 {} 下载专业归档模板文件成功: {}", loginUser.getRealName(), fileName);
        } catch (Exception e) {
            log.error("用户 {} 下载专业归档模板文件失败: {}", loginUser.getRealName(), e.getMessage());
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

//    @ApiOperation("专业归档负责人 获取负责的专业批次的文件信息")
//    @GetMapping("/batchFilesInfo")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "batchName", value = "评审批次", required = true, paramType = "query"),
//            @ApiImplicitParam(name = "majorId", value = "评审的专业id", required = true, paramType = "query")
//    })
//    public ResponseMessage<List<MajorArchiveGetBatchFilesInfoResult>> getBatchFilesInfo(@NotNull(message = "批次名为空") @RequestParam String batchName,
//                                                                                        @NotNull(message = "专业Id为空") @RequestParam Integer majorId) {
//        return ResponseMessage.newSuccessInstance(majorArchiveProcessService.getBatchFilesInfo(batchName, majorId));
//    }

    @GetMapping("/majorArchiveBatch/{majorId}")
    @ApiOperation("获取此专业的所有归档批次信息  (暂时不用) ")
    public ResponseMessage<List<MajorInfoResult>> getAllMajorArchiveInfo(@PathVariable("majorId") @NotNull(message = "专业Id为空") Integer majorId,
                                                                         @RequestParam @NotNull(message = "页数为空") Integer pageNum,
                                                                         @RequestParam @NotNull(message = "页大小为空") Integer pageSize) {
        return ResponseMessage.newSuccessInstance(majorArchiveService.getAllMajorInfo(majorId, pageNum, pageSize));
    }

    @GetMapping("/batchInfo/{majorId}")
    @ApiOperation("获取此专业的所有归档批次信息")
    public ResponseMessage<List<MajorArchiveBatchInfoVO>> getMajorArchiveBatchInfoList(@NotNull(message = "专业Id为空") @PathVariable("majorId") Integer majorId) {
        return ResponseMessage.newSuccessInstance(majorArchiveProcessService.getMajorArchiveInfoList(majorId));
    }


    @GetMapping("/majorArchiveOpinion/{batchId}")
    @ApiOperation("获取该专业归档批次 所有的评审意见")
    public ResponseMessage<List<MajorArchiveOpinionResult>> getMajorArchiveOpinion(@PathVariable("batchId") @NotNull(message = "专业归档表为空") @ApiParam("专业归档表id") String batchId) {
        return ResponseMessage.newSuccessInstance(majorArchiveService.getMajorArchiveOpinion(Long.valueOf(batchId)));
    }

    @ApiOperation("专业归档负责人 根据专业批次提交评审")
    @PostMapping("/submitEvaluation")
    public ResponseMessage<String> submitEvaluation(@Validated @RequestBody MajorArchiveReviewBO majorArchiveReviewBO) {
        return majorArchiveProcessService.submitEvaluation(majorArchiveReviewBO) ? ResponseMessage.newSuccessInstance("评审意见提交成功") : ResponseMessage.newSuccessInstance("评审意见提交失败");
    }

    @PostMapping("/endEvaluation/{batchId}")
    @ApiOperation("结束评审流程")
    public ResponseMessage<String> setEvaluationEnd(@PathVariable("batchId") @NotNull(message = "专业归档表为空") @ApiParam("专业归档表id") String batchId) {
        return majorArchiveService.setEvaluationEnd(Long.valueOf(batchId)) ? ResponseMessage.newSuccessInstance("评审流程结束") : ResponseMessage.newErrorInstance("请重试结束评审流程");
    }


    @PostMapping("/adminGroupLeader")
    @ApiOperation("（后台）将某用户设置为评审小组组长，并设置小组名和添加评审的专业和批次(暂时不用)")
    public ResponseMessage<String> takeGroupLeader(@NotNull(message = "用户id为空") @ApiParam("用户id") Integer userId,
                                                   @NotNull(message = "小组名字为空") @ApiParam("小组名字") String groupName,
                                                   @NotNull(message = "专业名字为空") @ApiParam("专业名字") String majorName,
                                                   @NotNull(message = "批次名字为空") @ApiParam("批次名字") String batchName) {
        return majorArchiveService.takeGroupLeader(userId, groupName, majorName, batchName) ? ResponseMessage.newSuccessInstance("设置成功") : ResponseMessage.newErrorInstance("信息有误，设置失败");
    }

    @PostMapping("/addGroupUser")
    @ApiOperation("组长添加评审小组成员(暂时不用)")
    public ResponseMessage<String> takeGroupUser(@NotNull(message = "用户id为空") @ApiParam("用户id") Integer userId,
                                                 @NotNull(message = "小组id为空") @ApiParam("小组id") Integer groupId) {

        if (UserUtil.getCurrentUser().getId().equals(userId)) {
            return ResponseMessage.newErrorInstance("不能添加自己");
        }

        if (UserUtil.getCurrentUser().getId().equals(majorArchiveService.getGroupLeader(groupId))) {
            return ResponseMessage.newErrorInstance("你不是改组的组长");
        }
        return majorArchiveService.takeGroupUser(userId, groupId) ? ResponseMessage.newSuccessInstance("添加成功") : ResponseMessage.newErrorInstance("信息有误，添加失败");
    }

    @GetMapping("/getGroupUser")
    @ApiOperation("获取某小组的成员(暂时不用)")
    public ResponseMessage<List<UserInfoResult>> getGroupUser(@NotNull(message = "小组id为空") @ApiParam("小组id") Integer groupId) {
        return ResponseMessage.newSuccessInstance(majorArchiveService.getGroupUser(groupId));
    }

    @PostMapping("/adminAddGroupUser")
    @ApiOperation("（后台）添加评审小组成员(暂时不用)")
    public ResponseMessage<String> adminAddGroupUser(@NotNull(message = "用户id为空") @ApiParam("用户id") Integer userId,
                                                     @NotNull(message = "小组id为空") @ApiParam("小组id") Integer groupId) {
        return majorArchiveService.takeGroupUser(userId, groupId) ? ResponseMessage.newSuccessInstance("添加成功") : ResponseMessage.newErrorInstance("信息有误，添加失败");
    }

    @GetMapping("/getAllGroupInfo")
    @ApiOperation("获取所有小组及其成员(暂时不用)")
    public ResponseMessage<List<GroupInfoResult>> getAllGroupInfo() {
        return ResponseMessage.newSuccessInstance(majorArchiveService.getAllGroupInfo());
    }

    @PostMapping("/adminDeleteGroupUser")
    @ApiOperation("（后台）删除评审小组成员(暂时不用)")
    public ResponseMessage<String> adminDeleteGroupUser(@NotNull(message = "用户id为空") @ApiParam("用户id") Integer userId,
                                                        @NotNull(message = "小组id为空") @ApiParam("小组id") Integer groupId) {
        return majorArchiveService.deleteGroupUser(userId, groupId) ? ResponseMessage.newSuccessInstance("删除成功") : ResponseMessage.newErrorInstance("数据为空或者信息有误");
    }

    @PostMapping("/adminDeleteGroup")
    @ApiOperation("（后台）删除评审小组(暂时不用)")
    public ResponseMessage<String> adminDeleteGroup(@NotNull(message = "小组id为空") @ApiParam("小组id") Integer groupId) {
        return majorArchiveService.deleteGroup(groupId) ? ResponseMessage.newSuccessInstance("删除成功") : ResponseMessage.newErrorInstance("数据为空或者信息有误");
    }

    @GetMapping("/getGroupInfo")
    @ApiOperation("组员（包括组长）获取自己所有小组的信息(暂时不用)")
    public ResponseMessage<List<GroupInfoResult>> getGroupInfo() {
        User user = UserUtil.getCurrentUser();
        return ResponseMessage.newSuccessInstance(majorArchiveService.getGroupInfo(user.getId()));
    }

}
