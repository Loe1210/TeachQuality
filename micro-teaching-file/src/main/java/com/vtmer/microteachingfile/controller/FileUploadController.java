package com.vtmer.microteachingfile.controller;

import com.hung.microoauth2commons.commonutils.api.Result;
import com.vtmer.microteachingfile.model.dto.CompleteUploadSessionRequest;
import com.vtmer.microteachingfile.model.dto.CreateUploadSessionRequest;
import com.vtmer.microteachingfile.model.pojo.FileObject;
import com.vtmer.microteachingfile.model.vo.CreateUploadSessionResponse;
import com.vtmer.microteachingfile.model.vo.FileObjectResponse;
import com.vtmer.microteachingfile.model.vo.UploadSessionDetailResponse;
import com.vtmer.microteachingfile.service.UploadSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @ApiOperation("下载文件对象")
    @GetMapping("/objects/{fileObjectId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFileObject(@PathVariable Long fileObjectId) throws IOException {
        FileObject fileObject = uploadSessionService.getReadyFileObject(fileObjectId);
        Path filePath = uploadSessionService.getFileObjectPath(fileObjectId);
        org.springframework.core.io.Resource resource = new FileSystemResource(filePath);
        String encodedName = URLEncoder.encode(fileObject.getOriginalName(), StandardCharsets.UTF_8);
        String contentType = fileObject.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = Files.probeContentType(filePath);
        }
        MediaType mediaType = contentType == null || contentType.isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encodedName)
                .contentType(mediaType)
                .contentLength(Files.size(filePath))
                .body(resource);
    }
}
