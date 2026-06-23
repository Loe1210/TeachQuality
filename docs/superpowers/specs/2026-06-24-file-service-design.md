# 独立文件服务设计说明

## 背景

当前教学质量项目的文件上传链路仍然是 `MultipartFile` 直传，并在业务服务内部通过 `file.getBytes()` + `FileUtil.writeBytes(...)` 直接写入本地磁盘。该实现已经可以支撑中小型评审材料上传闭环，但不适合大量大文件并发上传，主要问题是：

- 业务服务和文件流量强耦合
- 请求生命周期过长
- JVM 内存压力大
- 本地磁盘 IO 成为业务瓶颈
- 缺少分片上传、断点续传、去重、失败补偿、对象存储抽象

本次改造目标是新增独立的 `micro-teaching-file` 服务，承接大文件上传链路，并让教学质量服务从“直接接收文件流”切换到“绑定 fileObjectId / uploadSessionId”。

## 目标

本次设计需要落地以下能力：

- 分片上传
- 断点续传
- 上传会话管理 `upload_session`
- 临时分片区
- 分片合并
- 文件 hash 去重
- MIME / 魔数校验
- 存储抽象，预留 MinIO / OSS / S3
- 超大文件场景下的限流、超时清理、失败补偿
- 与现有教学质量业务服务的对接

## 非目标

本次不做以下内容：

- 前端上传 SDK
- 真正接入 MinIO / OSS / S3 生产环境
- 跨服务分布式事务框架
- 完整的运维后台页面

## 模块划分

新增模块：

- `micro-teaching-file`

职责：

- 管理上传会话
- 接收和落盘分片
- 校验 MIME / 魔数 / 大小 / hash
- 合并临时分片
- 管理文件对象元数据
- 提供文件查询与删除能力
- 提供清理超时会话与临时分片的定时任务

保留在 `micro-teaching-quality` 的职责：

- 课程 / 专业评审流程状态推进
- 业务对象和文件对象的绑定
- 上传完成后的业务通知和 RocketMQ 事件发送

## 数据模型

### 1. `file_object`

表示最终完成上传、可供业务引用的文件对象。

核心字段：

- `id`
- `biz_type`
- `biz_id`
- `original_name`
- `stored_name`
- `storage_path`
- `storage_provider`
- `file_size`
- `content_type`
- `file_hash`
- `status`
- `create_user_id`
- `create_time`
- `update_time`

状态建议：

- `INIT`
- `UPLOADING`
- `MERGING`
- `READY`
- `FAILED`
- `DELETED`

### 2. `upload_session`

表示一次上传会话，用于断点续传和状态查询。

核心字段：

- `id`
- `session_code`
- `biz_type`
- `biz_id`
- `original_name`
- `expected_size`
- `chunk_size`
- `total_chunks`
- `uploaded_chunks`
- `client_file_hash`
- `server_file_hash`
- `content_type`
- `status`
- `temp_dir`
- `expire_time`
- `last_chunk_time`
- `create_user_id`
- `create_time`
- `update_time`

状态建议：

- `INIT`
- `UPLOADING`
- `MERGING`
- `COMPLETED`
- `FAILED`
- `CANCELED`
- `EXPIRED`

### 3. `upload_chunk`

表示单个分片的落盘和校验状态。

核心字段：

- `id`
- `session_id`
- `chunk_index`
- `chunk_size`
- `chunk_hash`
- `storage_path`
- `status`
- `upload_time`
- `retry_count`

状态建议：

- `PENDING`
- `UPLOADED`
- `MERGED`
- `FAILED`

## API 设计

### 文件服务接口

1. `POST /file/upload-sessions`
- 初始化上传
- 入参：业务类型、业务 ID、原文件名、总大小、chunkSize、totalChunks、contentType、可选 fileHash
- 出参：`sessionId`、`sessionCode`、建议 chunkSize、已上传分片列表

2. `GET /file/upload-sessions/{sessionId}`
- 查询上传会话
- 返回会话状态、已上传分片、缺失分片、是否可完成

3. `PUT /file/upload-sessions/{sessionId}/chunks/{chunkIndex}`
- 上传单个分片
- 使用流式写入，不一次性读入 JVM

4. `POST /file/upload-sessions/{sessionId}/complete`
- 触发完整性校验和合并
- 成功后生成 `fileObjectId`

5. `DELETE /file/upload-sessions/{sessionId}`
- 取消上传，删除临时分片

6. `GET /file/objects/{fileObjectId}`
- 查询文件对象元数据

7. `DELETE /file/objects/{fileObjectId}`
- 逻辑删除文件对象，并尝试清理物理文件

### 教学质量服务接口改造

原有：

- 直接接收 `MultipartFile`

改造后：

- 先由前端调用文件服务完成上传
- 教学质量服务接收 `fileObjectId` 或 `sessionId`
- 校验文件对象状态为 `READY`
- 建立业务对象和文件对象关联
- 推进流程状态并发送 RocketMQ 通知

## 存储抽象

定义统一接口：

- `FileStorageService`

建议方法：

- `saveChunk(...)`
- `mergeChunks(...)`
- `deleteChunk(...)`
- `deleteObject(...)`
- `openStream(...)`
- `exists(...)`

本次默认实现：

- `LocalFileStorageService`

目录结构：

- `storage/file-service/temp/{sessionCode}/chunk-{index}.part`
- `storage/file-service/object/{yyyyMMdd}/{storedName}`

预留实现：

- `MinioFileStorageService`
- `OssFileStorageService`
- `S3FileStorageService`

## 校验设计

### MIME 校验

- 使用上传时的 `contentType`
- 同时配合服务端探测
- 不只信任前端传参

### 魔数校验

- 读取文件头若干字节识别类型
- 当前至少支持：
  - pdf
  - doc
  - docx
  - xls
  - xlsx
  - zip
  - png
  - jpg

### hash 去重

- 优先使用前端传来的整文件 hash
- 合并完成后服务端再次计算 hash
- 如果 hash 已存在且文件状态为 `READY`，则直接复用已有 `file_object`

## 大文件并发治理

### 限流

- 每用户同时进行中的上传会话数限制
- 每会话同时分片上传数限制
- 单分片大小上限
- 单文件总大小上限

### 超时处理

- 初始化会话后设置 `expire_time`
- 长时间未继续上传的会话自动标记 `EXPIRED`
- 定时任务清理超时临时分片

### 失败补偿

- 分片上传失败允许重传单片
- 合并失败保留会话状态和临时分片，允许重试 complete
- 超过最大重试次数后标记 `FAILED`

## 与 RocketMQ 的关系

文件服务本身不负责评审业务通知。

链路是：

1. 前端完成分片上传
2. 文件服务完成合并并生成 `fileObjectId`
3. 前端或业务服务再调用教学质量服务进行业务绑定
4. 教学质量服务在绑定成功后推进评审流程
5. 教学质量服务发送 RocketMQ 事件

这样可以保证：

- 文件上传问题不会直接阻塞业务主服务的文件流接收能力
- RocketMQ 仍然只负责评审事件通知，不承担大文件传输

## 测试策略

至少覆盖：

- 初始化上传会话
- 分片上传成功 / 重复上传 / 越界上传
- 断点续传查询
- 合并成功
- hash 去重复用
- MIME / 魔数校验失败
- 会话超时清理
- 教学质量服务绑定 `fileObjectId`

## 分阶段实施顺序

1. 新增 `micro-teaching-file` 模块并接入根工程
2. 建三张核心表和基础实体 / Mapper / Service
3. 实现本地存储抽象和上传会话接口
4. 实现分片上传、进度查询、完成合并
5. 实现校验、去重、清理任务、限流规则
6. 改造教学质量服务上传接口，切换为 `fileObjectId` 绑定
7. 跑构建验证并补开发记录

## 风险与权衡

- 本次直接拆独立服务，改动面会大于“在原服务内先做文件域”方案
- 由于当前项目历史代码中大量上传接口仍耦合 `MultipartFile`，替换时需要分步骤推进
- 当前先做本地存储实现，后续接对象存储时仍需再做一轮迁移设计
