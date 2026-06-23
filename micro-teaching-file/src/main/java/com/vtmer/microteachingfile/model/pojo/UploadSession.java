package com.vtmer.microteachingfile.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_session")
public class UploadSession {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String sessionCode;

    private String bizType;

    private Long bizId;

    private String originalName;

    private Long expectedSize;

    private Long chunkSize;

    private Integer totalChunks;

    private Integer uploadedChunks;

    private String clientFileHash;

    private String serverFileHash;

    private String contentType;

    private Integer status;

    private String tempDir;

    private Long fileObjectId;

    private Integer completeRetryCount;

    private LocalDateTime expireTime;

    private LocalDateTime lastChunkTime;

    private Integer createUserId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
