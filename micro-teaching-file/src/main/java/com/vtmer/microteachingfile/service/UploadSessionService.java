package com.vtmer.microteachingfile.service;

import com.vtmer.microteachingfile.model.dto.CompleteUploadSessionRequest;
import com.vtmer.microteachingfile.model.dto.CreateUploadSessionRequest;
import com.vtmer.microteachingfile.model.pojo.FileObject;
import com.vtmer.microteachingfile.model.vo.CreateUploadSessionResponse;
import com.vtmer.microteachingfile.model.vo.FileObjectResponse;
import com.vtmer.microteachingfile.model.vo.UploadSessionDetailResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface UploadSessionService {

    CreateUploadSessionResponse createSession(CreateUploadSessionRequest request);

    UploadSessionDetailResponse getSession(Long sessionId);

    UploadSessionDetailResponse uploadChunk(Long sessionId, Integer chunkIndex, MultipartFile file, String chunkHash, Integer operatorUserId);

    FileObjectResponse completeSession(Long sessionId, CompleteUploadSessionRequest request);

    void cancelSession(Long sessionId, Integer operatorUserId);

    FileObjectResponse getFileObject(Long fileObjectId);

    void deleteFileObject(Long fileObjectId, Integer operatorUserId);

    FileObject getReadyFileObject(Long fileObjectId);

    Path getFileObjectPath(Long fileObjectId);
}
