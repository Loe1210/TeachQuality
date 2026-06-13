# 教学质量监测项目文档与代码差距分析

## 分析范围

- 需求来源：`教学质量监测项目拷打.docx`
- 代码范围：
  - `micro-teaching-quality`
  - `micro-oauth2-auth`
  - `micro-oauth2-gateway`
  - `micro-teaching-message`

## 结论摘要

当前仓库已经实现了“课程/专业评审流程、材料上传、专家打分、组长复核、通知消费者、JWT 网关鉴权”的基础骨架，但文档里描述的很多能力仍然没有真正落地，或者只停留在半成品状态。差距主要集中在四类：

1. 文档里的工程化能力未实现：Sentinel、Bloom Filter、本地消息表、消息重试/死信、文件分片/断点续传、文件服务独立拆分。
2. 部分链路只有消费端，没有稳定的生产端：RocketMQ 发送代码大面积被注释，通知闭环不成立。
3. 认证与网关能力不完整：只有发 token 和基础 RBAC，没有注销黑名单、refresh 续期、tokenVersion/permissionVersion 等生命周期管理。
4. 业务流程存在明显未完工点：存在 `作废`、`暂时不用`、`不使用`、`TODO`、硬编码用户 ID、直接 `return false` 的实现。

## 文档需求与代码现状对照

### 1. 微服务拆分与文件服务隔离

文档描述：
- 系统拆分为网关、认证、评审、文件、消息 5 个服务。
- 文件服务独立承接上传下载、预览与元数据。

代码现状：
- 仓库中有 `micro-oauth2-auth`、`micro-oauth2-gateway`、`micro-teaching-message`、`micro-teaching-quality`，但没有独立的文件服务模块。
- 文件上传仍直接落在 `micro-teaching-quality` 内部，且直接写本地磁盘。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/ClazzEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/MajorEvaluationProcessServiceImpl.java`
- 这两个服务都直接使用 `FileUtil.writeBytes(...)` 和本地目录路径。

结论：
- “文件服务独立拆分”未实现。
- 当前仍是业务服务直接处理文件 IO，和文档描述不一致。

### 2. Gateway 三层防护与流量治理

文档描述：
- Gateway + Sentinel 做多维限流、熔断、降级。
- Bloom Filter 预判非法 `courseId/taskId/fileId`。
- JWT + Redis 黑名单 + RBAC 做统一鉴权。

代码现状：
- Gateway 已有基础 JWT 资源服务器能力和 RBAC 鉴权。
- 仅看到 Spring Cloud Gateway 的 `RequestRateLimiter` 配置，没有 Sentinel 相关依赖、规则、降级处理。
- 未发现 Bloom Filter 相关实现。
- 未发现 Redis 黑名单、tokenVersion、permissionVersion 的实现。

证据：
- `micro-oauth2-gateway/src/main/java/com/hung/microoauth2gateway/config/WebFluxSecurityConfig.java`
- `micro-oauth2-gateway/src/main/java/com/hung/microoauth2gateway/authorization/AuthorizationManager.java`
- `micro-oauth2-gateway/src/main/java/com/hung/microoauth2gateway/constant/RedisConstant.java`
- `micro-oauth2-gateway/src/main/resource/application.yml`

结论：
- 已实现：JWT 资源服务器接入、白名单放行、基于 Redis 的资源角色映射鉴权。
- 未实现：Sentinel 熔断限流体系、Bloom Filter、Redis token 黑名单、版本化 token 失效机制。

### 3. 认证生命周期管理

文档描述：
- 支持 JWT 注销、Redis 黑名单、refreshToken 续期、权限变更后的旧 token 失效。

代码现状：
- Auth 服务只有获取 token 接口。
- `AuthController` 返回 access token 和 refresh token，但没有注销、刷新、撤销、黑名单写入等接口。
- 业务服务切面里排除了 `UserController.logout(..)`，但 `UserController` 里根本没有 `logout` 方法。

证据：
- `micro-oauth2-auth/src/main/java/com/hung/microoauth2auth/controller/AuthController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/UserController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/common/config/AspectConfig.java`

结论：
- “JWT 生命周期治理”只完成了发 token，没完成收 token。
- 文档里的注销黑名单、强制下线、权限变更即时失效均未落地。

### 4. RocketMQ 削峰与可靠投递

文档描述：
- 评审任务/通知通过 RocketMQ 异步削峰。
- 使用本地消息表 `msg_log` 保证最终一致性。
- 定时重试、失败转人工、死信队列兜底。

代码现状：
- `micro-teaching-message` 已经有消费者监听器，说明消费侧骨架存在。
- 但 `micro-teaching-quality` 中大量 RocketMQ 发送代码被注释掉，生产侧未真正启用。
- 未发现 `msg_log`、本地消息表实体/mapper、事务消息、定时补偿任务、死信处理代码。

证据：
- `micro-teaching-message/src/main/java/com/vtmer/microteachingmessage/listener/ClazzEvaluationProcessListener.java`
- `micro-teaching-message/src/main/java/com/vtmer/microteachingmessage/listener/MajorEvaluationProcessListener.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/ClazzEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/MajorEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/CourseEvaluationExpertServiceImpl.java`

结论：
- “异步消息消费者”有。
- “可靠消息生产链路”基本没有完成，当前更像是把 MQ 预埋了，但没有真正打通。

### 5. 文件上传增强能力

文档描述：
- 分片上传、断点续传、临时区机制。
- MIME/魔数校验、Hash 去重。
- 失败状态补偿。
- 文件接口限流与超时保护。

代码现状：
- 上传接口仍是单文件 `MultipartFile` 直传。
- 未看到分片上传、合并分片、断点续传、临时分片记录表。
- 未看到 MIME/魔数校验、文件 hash 去重、上传补偿任务。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/ClazzEvaluationProcessController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/MajorEvaluationProcessController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/ClazzEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/MajorEvaluationProcessServiceImpl.java`

结论：
- 文件上传只有“能传”这一层，文档里的工程化增强几乎都没做。

### 6. 业务流程仍有未完成点

发现的问题：
- 多个接口被标记为 `作废`、`暂时不用`、`不使用`、`这个功能先放着`。
- 专业评审提交中出现硬编码 `Integer userId = 83;`。
- 更新专家评审的方法未完成，直接 `return false;`。
- 课程评审提交逻辑仍保留“前端不知道用户 id，需要重新设计”的 TODO。
- 多处上传逻辑保留“上传文件不会插入 id”的 TODO。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/ClazzEvaluationProcessController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/MajorEvaluationProcessController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/MajorEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/CourseEvaluationExpertServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/ClazzEvaluationProcessServiceImpl.java`

结论：
- 当前业务功能并非“文档描述的完整线上闭环”，而是“主流程可用，但存在多个未收口点”。

## 我认为最需要优先补齐的缺口

## 代码合理性与风格问题

除了“文档功能未实现”之外，当前项目还有一批明显不合理、维护成本高或风格不统一的问题，这些也应该纳入后续修改计划。

### 1. 敏感配置和环境信息硬编码

发现的问题：
- Nacos、MySQL、Redis、RocketMQ 地址直接写在配置文件里。
- Redis / 数据库密码明文出现在仓库配置中。
- JWT keystore 密码硬编码为 `123456`。

证据：
- `micro-teaching-quality/src/main/resources/application.yml`
- `micro-teaching-quality/src/main/resources/application-test.yml`
- `micro-teaching-quality/src/main/resources/application-prod.yml`
- `micro-oauth2-gateway/src/main/resource/application.yml`
- `micro-oauth2-gateway/src/main/java/com/hung/microoauth2gateway/config/TokenConfig.java`
- `micro-oauth2-auth/src/main/java/com/hung/microoauth2auth/config/Oauth2AuthorizationServerConfig.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/auth/config/TokenConfig.java`

结论：
- 这是安全和可运维性问题，不只是代码风格问题。
- 应改为环境变量、外部配置中心密文、或至少本地覆盖配置，不应把生产敏感信息提交进仓库。

### 2. HTTP 语义混乱，接口设计不规范

发现的问题：
- 存在 `GET /create`、`GET /delete/...` 这类会修改数据的接口。
- 删除操作有时用 `POST`，有时用 `GET`，有时用 `DELETE`，风格不统一。
- 同一类资源的 URL 设计不一致，后续前后端联调和权限治理会越来越乱。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/RoleController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/MajorArchiveController.java`

结论：
- 这会直接影响网关限流、幂等判断、审计日志和前端调用习惯。
- 应统一 REST 语义：查询 `GET`、创建 `POST`、更新 `PUT/PATCH`、删除 `DELETE`。

### 3. 控制器过重，下载/文件处理逻辑大量复制

发现的问题：
- 多个 Controller 手写了几乎一样的下载逻辑：解密路径、拼 header、打开流、循环写出、finally 关闭流。
- 有些类甚至写了通用 `download(...)` 私有方法，但并没有真正复用。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/ReportController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/MajorArchiveController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/MajorEvaluationProcessController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/ClazzEvaluationProcessController.java`

结论：
- 文件下载应抽到统一组件或 `DownloadUtil`，控制器只保留鉴权和参数校验。
- 现在这种复制写法很容易修一处漏三处。

### 4. 异常处理过宽，错误语义不稳定

发现的问题：
- 大量 `catch (Exception e)`，但只打印 `e.getMessage()`，没有区分业务异常、IO 异常、权限异常。
- 部分方法吞异常后仍返回成功，或者返回泛化错误信息。
- 大量 `return null;` / `return false;` 作为失败路径，调用方很难知道失败原因。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/UserController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/MajorArchiveController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/ReportController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/RoleServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/ReportServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/NotifyServiceImpl.java`

结论：
- 这会导致前端很难处理失败分支，也让排查线上问题变得非常痛苦。
- 建议统一异常模型：业务异常直接抛出，自定义异常码；IO 和系统异常做统一包装。

### 5. 领域逻辑里存在硬编码、魔法值和可读性差的问题

发现的问题：
- 存在硬编码用户 ID、固定密钥、魔法数字状态值、固定年份过滤、固定字符串状态描述。
- 状态推进依赖多个 `if (status == 2)` 分支串联，可读性和可维护性都比较弱。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/MajorEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/ClazzEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/util/EncryptUtil.java`

结论：
- 这类问题短期看只是“丑”，长期会演变成流程 bug 温床。
- 需要把状态值、角色值、流程阶段、固定文案统一抽成枚举/常量/状态机。

### 6. 数据访问风格混杂，AR 与 Mapper 混用严重

发现的问题：
- 同一项目中同时大量使用：
  - `entity.insert()`
  - `entity.update(updateWrapper)`
  - `mapper.insert(...)`
  - `mapper.deleteById(...)`
  - `selectByPrimaryKey(...)`
  - `selectById(...)`
- 这让事务边界、可测试性和代码风格都很分裂。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/ClazzEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/MajorEvaluationProcessServiceImpl.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/service/impl/ReportServiceImpl.java`

结论：
- 建议统一为 Service + Mapper 风格，减少 ActiveRecord 式写法。
- 同时逐步淘汰 `selectByPrimaryKey` 这类老式命名，统一成 `selectById` / `findBy...`。

### 7. 权限判断和业务判断耦合，容易写反

发现的问题：
- 很多地方把角色判断、用户类型判断、数据归属判断混在控制器里。
- 有些逻辑明显容易写反，例如“你不是该组的组长”的判断代码可读性就很差。
- 权限逻辑散落在 Controller、Service、Gateway 中，没有统一约束层。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/MajorArchiveController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/ReportController.java`
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/controller/MajorEvaluationProcessController.java`

结论：
- 建议把“接口权限”和“数据权限”拆开管理。
- Controller 只做入口权限，Service 层沉淀可复用的归属校验方法。

### 8. AOP 日志切面过脆弱

发现的问题：
- `AspectConfig` 里排除了 `UserController.logout(..)`、`login(..)` 等方法，但这些方法并不都存在。
- 切面直接假设 `SecurityContext` 一定有用户，一旦匿名访问或认证异常就可能报错。
- 参数日志直接把所有参数拼接输出，可能引入敏感信息泄露风险。

证据：
- `micro-teaching-quality/src/main/java/com/vtmer/microteachingquality/common/config/AspectConfig.java`

结论：
- 日志切面需要改成更稳的判空方式，并对密码、token、验证码等字段脱敏。

### 9. 测试质量偏弱

发现的问题：
- 有不少测试类仍保留 `//TODO: Test goes here...`
- 一些测试更像临时脚本，依赖本地文件或真实环境，不适合作为稳定回归测试。
- 当前缺少对关键业务流程的自动化验证。

证据：
- `micro-teaching-quality/src/test/java/...`

结论：
- 如果要进入持续迭代，必须补关键服务层测试，至少覆盖：
  - 评审流程状态推进
  - 上传/删除文件记录
  - MQ 事件发送
  - 认证与权限失败分支

## 代码治理类修改计划

### P0.1 安全与配置治理

- 移除仓库中的明文密码、固定地址、固定 keystore 密码。
- 改为环境变量或外部配置注入。
- 清理示例配置，提供 `application-example.yml`。

### P0.2 接口语义与权限入口治理

- 统一 Controller 的 HTTP Method 语义。
- 清理 `GET create/delete` 这类反模式接口。
- 统一参数风格：路径参数、查询参数、请求体的职责边界。

### P1.1 文件与下载逻辑收敛

- 抽统一文件下载组件。
- 抽统一文件路径解密/校验逻辑。
- 控制器去掉重复的流处理代码。

### P1.2 异常与返回模型收敛

- 减少 `catch (Exception e)`。
- 用明确异常类型和统一异常码替代 `return null/false`。
- 梳理“失败却返回成功文案”的接口。

### P1.3 领域代码去硬编码

- 清理硬编码用户 ID、固定年份、固定状态文案、固定密钥。
- 用枚举、配置项、常量类和状态推进方法替代散落魔法值。

### P2.1 数据访问风格统一

- 逐步取消 ActiveRecord 风格 `entity.insert()/update()/delete()`。
- 统一到 Mapper + Service 模式。
- 对外暴露更清晰的仓储/查询方法名。

### P2.2 权限校验收敛

- 提炼统一的“角色校验 + 归属校验”组件。
- 减少 Controller 中的权限分支散落。
- 为关键操作补充显式审计日志。

### P2.3 测试补强

- 删除脚本式、环境耦合强的伪测试。
- 补 service 层和关键 controller 的回归测试。
- 至少覆盖主流程、异常分支和权限分支。

### P0. 先修掉直接影响正确性的功能缺陷

- 去掉专业评审中的硬编码 `userId = 83`，统一改为当前登录用户。
- 完成 `updateMasterEvaluation(...)`，避免接口名存在但功能不可用。
- 梳理所有 `作废/暂时不用/不使用` 的接口，区分：
  - 真要上线的，补完实现。
  - 不再使用的，标记废弃并从路由/前端调用中移除。
- 修复上传记录主键/回写问题，确认上传文件元数据完整持久化。

### P1. 打通“评审事件 -> 通知消息”闭环

- 恢复并统一 RocketMQ 生产端发送逻辑，不再让发送代码停留在注释里。
- 抽一个消息发送门面，统一封装 topic、tag、payload、日志。
- 对“创建流程、负责人上传、专家提交、组长提交、退回评审”等关键动作统一发事件。
- 校验 `micro-teaching-message` 消费者的 tag 兼容性，避免生产消费不一致。

### P2. 补齐可靠消息能力

- 新增本地消息表，例如 `msg_log`。
- 业务落库和消息入表放到同一事务。
- 消息发送成功后更新状态为 `SUCCESS`。
- 增加定时补偿任务，扫描 `SENDING/FAIL` 状态并重试。
- 为消费侧补幂等保护，避免重复消费造成通知重复或流程错乱。
- 形成失败告警和人工处理入口。

### P3. 补齐认证生命周期与网关安全能力

- 在 Auth 或用户服务中新增：
  - 退出登录
  - refresh token 刷新
  - 强制下线
  - 权限变更后失效
- Redis 增加：
  - token 黑名单
  - `tokenVersion` / `permissionVersion`
- Gateway 鉴权补充：
  - 黑名单校验
  - 版本校验
  - 高风险接口降级策略

### P4. 补齐文件链路的工程化能力

- 先在现有服务内抽象文件域服务，隔离业务流程与物理存储。
- 增加文件元数据表字段：hash、mime、size、status、uploadSessionId。
- 引入：
  - MIME/魔数校验
  - Hash 去重
  - 上传状态机
  - 失败补偿
- 第二阶段再决定是否真正拆分独立文件微服务。
- 如果近期确实有大文件场景，再做分片上传与断点续传。

### P5. 再补 Sentinel / Bloom Filter / 高并发防护

- 这是文档中的“亮点能力”，但不是当前最紧急的正确性问题。
- 推荐在主流程稳定后补齐：
  - Sentinel 限流熔断规则
  - 关键资源 ID 的 Bloom Filter
  - 登录/上传/下载/提交等接口的差异化流控

## 建议的后续实施顺序

1. 先做业务正确性修复。
2. 立刻清理敏感配置和明显不合理的接口设计。
3. 再做 MQ 生产闭环。
4. 然后补可靠消息与认证生命周期。
5. 再做文件链路增强和通用下载组件收敛。
6. 最后补网关高级防护能力与代码风格统一。

## 下一轮修改建议

下一轮可以直接按下面顺序开始动手：

1. 修 `MajorEvaluationProcessServiceImpl` 的硬编码用户 ID 和未完成方法。
2. 清理明文配置、硬编码密钥和 `GET create/delete` 这类接口语义问题。
3. 修课程/专业评审里所有被注释掉的 RocketMQ 发送链路。
4. 补一个最小可用的 `msg_log` 可靠消息方案。
5. 增加 logout + token 黑名单能力。
6. 抽取统一文件下载/异常处理组件。

## 备注

- 本次分析基于当前仓库能看到的后端代码完成。
- 如果前端仓库、数据库表结构脚本、部署脚本在别处，部分“未实现”也可能只是“当前仓库未体现”，后续可以再做一次全链路复核。

## 开发记录

### 2026-06-13 模块 01：P0 专业评审正确性修复

- 分支：`feat/p0-major-evaluation-correctness`
- 范围：`MajorEvaluationProcessServiceImpl`

本次完成：
- 去掉了 `saveMasterEvaluation(...)` 中硬编码的 `userId = 83`，改为使用当前登录用户。
- 补完了 `updateMasterEvaluation(...)` 的核心逻辑，不再直接 `return false`。
- 把“首次提交评审”和“本人更新评审”收敛到同一套持久化逻辑，避免两份逻辑继续分叉。
- 修正了更新评审时错误地拿“流程创建者”判断权限的问题，改为基于“当前用户自己的评审记录”进行更新。

检查结论：
- 代码修改已完成并已推送到功能分支。
- 完整 Maven 校验受项目原有依赖和编码环境问题阻塞，未能在该步完全跑通。

### 2026-06-13 模块 02：P0 专业评审流程补强

- 分支：`feat/p0-evaluation-flow-hardening`
- 范围：`MajorEvaluationProcessServiceImpl`

本次完成：
- 收紧了专家评审首次提交逻辑：已提交过评审的用户不能再走提交接口，必须改用更新接口。
- 为专家评审提交、专家评审更新、组长评审都补上了 `optionMap` 非空校验，避免空评审入库。
- 修正了组长退回专家评审的错误对象：原实现错误地回退了 `principal_material_status`，现在改为回退对应专家的 `MasterEvaluation.status`，并把流程状态恢复到专家评审阶段。
- 为组长评审补上了阶段校验，避免在专家评审尚未完成时提前进入组长评审。

本次自检重点：
- 专家首次提交和专家更新的职责边界已区分清楚。
- 退回专家评审时，流程状态和专家记录状态会一起调整，不再误伤“负责人材料提交”阶段。
- 本模块仍受项目原有 Maven 构建问题影响，完整自动化验证需要等公共依赖和编码配置问题处理后再复测。
