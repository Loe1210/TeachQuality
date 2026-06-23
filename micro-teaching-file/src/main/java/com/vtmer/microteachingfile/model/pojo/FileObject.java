package com.vtmer.microteachingfile.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_object")
public class FileObject {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String bizType;

    private Long bizId;

    private String originalName;

    private String storedName;

    private String storagePath;

    private String storageProvider;

    private Long fileSize;

    private String contentType;

    private String fileHash;

    private Integer status;

    private Integer createUserId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
