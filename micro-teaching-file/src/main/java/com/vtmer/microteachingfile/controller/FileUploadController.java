package com.vtmer.microteachingfile.controller;

import com.hung.microoauth2commons.commonutils.api.Result;
import com.vtmer.microteachingfile.model.dto.CompleteUploadSessionRequest;
import com.vtmer.microteachingfile.model.dto.CreateUploadSessionRequest;
import com.vtmer.microteachingfile.model.vo.CreateUploadSessionResponse;
import com.vtmer.microteachingfile.model.vo.FileObjectResponse;
import com.vtmer.microteachingfile.model.vo.UploadSessionDetailResponse;
import com.vtmer.microteachingfile.service.UploadSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Api(tags = "文件上传接口")
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Resource
    private UploadSessionService uploadSessionService;

    @ApiOperation("初始化上传会话")
    @PostMapping("/upload-sessions")
    public Result<CreateUploadSessionResponse> createSession(@Validated @RequestBody CreateUploadSessionRequest request) {
        return Result.success(uploadSessionService.createSession(request));
    }

    @ApiOperation("查询上传会话状态")
    @GetMapping("/upload-sessions/{sessionId}")
    public Result<UploadSessionDetailResponse> getSession(@PathVariable Long sessionId) {
        return Result.success(uploadSessionService.getSession(sessionId));
    }

    @ApiOperation("上传单个分片")
    @PutMapping("/upload-sessions/{sessionId}/chunks/{chunkIndex}")
    public Result<UploadSessionDetailResponse> uploadChunk(@PathVariable Long sessionId,
                                                           @PathVariable Integer chunkIndex,
                                                           @RequestPart("file") MultipartFile file,
                                                           @RequestParam(required = false) String chunkHash,
                                                           @RequestParam Integer operatorUserId) {
        return Result.success(uploadSessionService.uploadChunk(sessionId, chunkIndex, file, chunkHash, operatorUserId));
    }

    @ApiOperation("完成上传并触发合并")
    @PostMapping("/upload-sessions/{sessionId}/complete")
    public Result<FileObjectResponse> completeSession(@PathVariable Long sessionId,
                                                      @Validated @RequestBody CompleteUploadSessionRequest request) {
        return Result.success(uploadSessionService.completeSession(sessionId, request));
    }

    @ApiOperation("取消上传会话")
    @DeleteMapping("/upload-sessions/{sessionId}")
    public Result<String> cancelSession(@PathVariable Long sessionId, @RequestParam Integer operatorUserId) {
        uploadSessionService.cancelSession(sessionId, operatorUserId);
        return Result.success("取消上传成功");
    }

    @ApiOperation("查询文件对象")
    @GetMapping("/objects/{fileObjectId}")
    public Result<FileObjectResponse> getFileObject(@PathVariable Long fileObjectId) {
        return Result.success(uploadSessionService.getFileObject(fileObjectId));
    }

    @ApiOperation("删除文件对象")
    @DeleteMapping("/objects/{fileObjectId}")
    public Result<String> deleteFileObject(@PathVariable Long fileObjectId, @RequestParam Integer operatorUserId) {
        uploadSessionService.deleteFileObject(fileObjectId, operatorUserId);
        return Result.success("删除文件成功");
    }
}
