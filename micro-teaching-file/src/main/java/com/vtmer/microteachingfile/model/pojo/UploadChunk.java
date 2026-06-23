package com.vtmer.microteachingfile.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_chunk")
public class UploadChunk {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;

    private Integer chunkIndex;

    private Long chunkSize;

    private String chunkHash;

    private String storagePath;

    private Integer status;

    private Integer retryCount;

    private LocalDateTime uploadTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
