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

### 2026-06-13 模块 03：P0 权限管理接口与服务收敛

- 分支：`feat/p0-role-api-hardening`
- 范围：`RoleController`、`RoleServiceImpl`

本次完成：
- 把角色权限分配接口补成更合理的 REST 入口：
  - 新建改为 `POST /role`
  - 删除改为 `DELETE /role/{evaluationUserId}`
- 为避免现有调用方直接断掉，暂时保留了旧版 `GET /role/create` 和 `GET /role/delete/{evaluationUserId}` 作为兼容入口，并显式标记为兼容旧版。
- 给 `RoleController` 补上了 `@PreAuthorize("hasAnyAuthority('all')")` 和参数校验入口，避免权限管理接口裸奔。
- 修正了 `RoleServiceImpl` 里“重复关系校验写反”的 bug：原实现会在“没有重复数据”时直接返回失败，导致创建权限逻辑异常。
- 修正了课程评审/课程复评查询名称时误用 `majorMapper.getMajorName(...)` 的问题，改为按课程 `clazzId` 查询课程名。
- 收敛了角色权限查询 VO 的组装逻辑，减少重复代码。
- 修正了更新接口返回“创建成功/创建失败”的错误文案，改为“修改成功/修改失败”。

本次自检重点：
- 角色权限新增接口现在既符合 HTTP 语义，也兼容旧前端调用路径。
- 课程权限查询结果不再出现“课程 ID 去查专业名”的错配。
- 重复权限关系会被正确拦截，不重复的数据可以正常创建。

### 2026-06-13 模块 04：P1 日志安全与敏感配置外置化

- 分支：`feat/p1-aspect-log-hardening`
- 范围：`AspectConfig`、`RedisConfig`、JWT `TokenConfig`、多模块 `application*.yml`

本次完成：
- 重写了 `AspectConfig` 的用户解析与参数日志逻辑：
  - 匿名请求或异常认证上下文不再因为直接取 `principal` 报错。
  - 去掉了对不存在的 `UserController.logout(..)` 排除条件，避免切面配置和实际代码继续漂移。
  - 接口参数日志改为按参数名输出，并对 `password/pwd/token/authorization/secret` 等敏感字段脱敏。
  - 日志内容增加长度裁剪，避免超长请求体直接刷屏。
- 把 `micro-oauth2-auth`、`micro-oauth2-gateway`、`micro-teaching-quality` 中 JWT keystore 口令和别名改为从配置读取，不再硬编码在 Java 代码里。
- 把多模块中的 Nacos、MySQL、Redis、RocketMQ、Druid 登录配置改成环境变量占位符，减少仓库内明文敏感信息。
- 把 `RedisConfig` 中写死的 Redisson 地址和密码改为复用 `spring.redis.*` 配置，避免应用内出现第二套独立明文 Redis 配置。

本次自检重点：
- 切面在未登录、认证异常、参数包含敏感信息的情况下都能更稳地记录日志。
- JWT 口令和中间件地址已从源码常量移出，后续部署可以直接走环境注入。
- 本模块仍然没有处理所有历史硬编码项，例如 `EncryptUtil`、`CorsConfig`、代码生成配置等，后续模块继续收敛。

### 2026-06-13 模块 05：P1 CORS 与工具类硬编码收敛

- 分支：`feat/p1-cors-encrypt-hardening`
- 范围：`common/auth CorsConfig`、`EncryptUtil`、`application.yml`

本次完成：
- 把 `CorsConfig` 中成组写死的域名白名单收敛成配置项 `app.cors.allowed-origins`，避免每次域名变化都改 Java 代码。
- 修正了认证侧 CORS 配置里 `allowCredentials(true)` 仍搭配 `*` 的不合法写法，改为按配置列表注入 `allowedOriginPattern`。
- 收敛了 MVC 侧跨域配置，去掉重复的多段 `registry.addMapping("/**")` 硬编码。
- 删除了 `EncryptUtil` 中仅用于本地演示的 `main(...)` 和固定示例值，避免工具类继续夹带无关调试逻辑。

本次自检重点：
- 跨域白名单现在可以通过环境变量统一配置，不再把历史域名和 IP 写死在源码里。
- 认证侧跨域配置和 `allowCredentials` 的组合更符合 Spring 的预期行为。
- `EncryptUtil` 保留纯工具职责，不再包含调试入口。

### 2026-06-13 模块 06：P1 MQ 生产端闭环打通

- 分支：`feat/p1-mq-producer-closure`
- 范围：课程/专业评审主流程、统一消息发送组件、`UserMessageDTO`

本次完成：
- 新增统一的 `EvaluationMessageProducer`，集中封装课程/专业评审事件消息发送，避免业务层继续手写 topic、tag 和序列化逻辑。
- 修正了 `micro-teaching-quality` 中 `UserMessageDTO` 只有默认 `Object.toString()` 的问题，改为显式 DTO 结构，保证发送 payload 可被消费端按 JSON 正确解析。
- 打通了专业评审主链的 5 类消息发送：
  - 创建流程
  - 负责人上传材料
  - 专家提交评审
  - 专家组长提交评审
  - 退回材料
- 打通了课程评审主链的 5 类消息发送：
  - 创建流程
  - 负责人上传材料
  - 专家提交评审
  - 专家组长提交评审
  - 退回材料
- 把原来大面积注释掉的 RocketMQ 发送逻辑，从“散落注释”收敛成统一组件调用。

本次自检重点：
- 生产端发出的 payload 现在与 `micro-teaching-message` 消费端 `JSON.parseObject(...)` 的解析方式兼容。
- 课程/专业评审主流程的通知消息闭环已真正接通，不再只是预埋 RocketMQ 依赖。
- 这一模块仍未引入 `msg_log`、重试补偿和死信兜底，这部分会作为下一独立模块继续补齐。

### 2026-06-13 模块 07：P1 Maven 构建链治理

- 分支：`feat/p1-build-chain-hardening`
- 范围：根工程 `pom.xml`、`micro-oauth2-commons` 坐标、各模块 commons 依赖声明

本次完成：
- 把 `micro-teaching-message` 补回根工程 `modules`，让当前仓库恢复成更完整的 Maven reactor。
- 把 `micro-oauth2-commons` 从独立坐标 `com.hung:micro-oauth2-commons:1.0.0.RELEASE` 收敛到根父工程体系下，统一为 `com.vtmer` + `1.0-SNAPSHOT`。
- 同步调整了 `micro-oauth2-auth`、`micro-oauth2-gateway`、`micro-teaching-quality`、`micro-teaching-message` 对 commons 的依赖坐标，避免单独模块构建时继续错误地把公共模块当成外部私服依赖。
- 升级根工程中的 druid 版本，优先规避 `1.2.6` 传递依赖中包含的非法 `systemPath` 配置问题。
- 补了一处在构建验证中暴露出的真实编译问题：将 `ClazzEvaluationProcessServiceImpl.principalUploadMaterial(...)` 恢复为项目现有的 `@SneakyThrows` 风格，避免文件写入抛出的受检异常阻断编译。

本次自检重点：
- `micro-oauth2-commons` 现在明确成为当前多模块工程的一部分，而不是“像外部私有 jar 一样被下载”。
- `mvn -pl micro-teaching-quality -am test` 已成功跑通，说明 commons 联动构建和 druid 依赖问题已收敛。
- 当前这次 Maven 成功里测试仍然是 `skipTests` 状态，后续如果要做真正回归验证，还需要单独打开测试执行。

### 2026-06-13 模块 08：P2 最小可靠消息方案

- 分支：`feat/p2-msg-log-reliability`
- 范围：`msg_log`、统一发送组件、重试任务、建表 SQL

本次完成：
- 新增 `msg_log` 实体、Mapper、Service，把评审事件消息从“直接发送”改成“先记录发送日志，再尝试发送”。
- 在 `EvaluationMessageProducer` 中补上发送状态回写：
  - 新建日志时标记为 `PENDING`
  - 发送成功后标记为 `SUCCESS`
  - 发送失败后标记为 `FAIL`，记录错误信息和下次重试时间
- 引入基于事务提交后的发送策略：如果当前业务方法存在事务，消息会在事务 `afterCommit` 后再真正发送，避免业务回滚但消息提前发出。
- 新增 `MessageLogRetryTask` 定时补偿任务，对失败或待发送日志进行批量重试。
- 补充了 `docs/sql/2026-06-13-msg-log.sql`，交付最小建表脚本。
- 开启了 `@EnableScheduling`，让补偿任务具备运行条件。

本次自检重点：
- 生产端现在具备最小可靠消息能力，消息发送失败不会直接丢失。
- 这一轮仍然没有做消费者幂等表或死信告警页，后续如果继续深化，会优先补消费幂等和人工处理入口。

### 2026-06-13 模块 09：P2 消费幂等与重复消费防护

- 分支：`feat/p2-message-consume-idempotency`
- 范围：`micro-teaching-message` 监听器、消费日志、建表 SQL

本次完成：
- 新增 `msg_consume_log` 实体、Mapper、Service，为 RocketMQ 消费端补上幂等日志。
- 引入统一的 `MessageConsumeLogService.consumeOnce(...)`，对同一条消息执行：
  - 成功消费过则直接跳过
  - 首次消费则记录 `PROCESSING`
  - 消费成功后标记 `SUCCESS`
  - 消费失败后标记 `FAIL`，保留错误信息，允许后续重试
- 把课程评审、专业评审两个 RocketMQ 监听器接入统一幂等层，避免同一消息重复落通知。
- 补充了 `docs/sql/2026-06-13-msg-consume-log.sql`，交付消费幂等日志建表脚本。

本次自检重点：
- 消费端现在具备“成功只执行一次、失败允许重试”的最小幂等保护。
- 这一轮还没有把生产端 `msg_log` 和消费端 `msg_consume_log` 串成统一运维面板，后续如果继续深化，可补死信告警和人工重放入口。

### 2026-06-13 模块 10：P3 token 黑名单与退出登录

- 分支：`feat/p3-token-blacklist-logout`
- 范围：`micro-oauth2-auth`、`micro-oauth2-gateway`

本次完成：
- 在 `AuthController` 新增 `DELETE /oauth/logout`，支持从 `Authorization: Bearer ...` 中提取 access token 并加入 Redis 黑名单。
- 黑名单 TTL 按 token 剩余有效期自动计算，避免注销记录无限堆积。
- 在 Gateway 前置过滤器中增加黑名单校验，命中黑名单时直接返回 `401`，不再继续后续 JWT 鉴权与业务转发。
- 统一补充 `AUTH:TOKEN_BLACKLIST:` Redis key 前缀，明确认证服务与网关的共享约定。

本次自检重点：
- 退出登录后，旧 token 会在剩余有效期内持续失效，而不是只能“客户端自己删掉 token”。
- 这一轮还没有补 `refresh token` 刷新和 `permissionVersion/tokenVersion` 版本化失效，后续模块继续补齐。

### 2026-06-13 模块 11：P3 权限变更后的旧 token 失效

- 分支：`feat/p3-permission-version-invalidation`
- 范围：`micro-oauth2-auth`、`micro-oauth2-gateway`、`micro-teaching-quality`

本次完成：
- 在 JWT 增强器里新增 `permissionVersion` 声明，并改为从当前登录主体 JSON 中解析用户 ID，避免继续依赖不匹配的 `SecurityUser` 类型假设。
- 在 Gateway 鉴权器中增加权限版本校验：当 token 内的 `permissionVersion` 落后于 Redis 最新版本时，直接拒绝访问。
- 在教学质量服务新增 `PermissionVersionService`，用于在角色权限发生变化时递增 Redis 中的权限版本号。
- 把管理员改角色、组长授予专家角色这两条实际会变更权限的入口接入版本递增逻辑。

本次自检重点：
- 角色权限一旦被修改，用户需要重新登录获取新 token，旧 token 不会继续带着过期权限工作。
- 这一轮还没有做 refresh token 的同步失效控制，如果后续继续深化，可以把 refresh token 也纳入版本校验。

### 2026-06-24 模块 12：P4 独立文件服务与大文件上传链路

- 分支：`feat/p4-file-service-large-upload`
- 范围：新增 `micro-teaching-file` 模块、根工程接入、建表 SQL、最小测试

本次完成：
- 新增独立文件服务 `micro-teaching-file`，把“大文件上传能力”从 `micro-teaching-quality` 中拆出来，形成独立微服务骨架。
- 完成三张核心表对应的实体、Mapper、Service 和接口设计：
  - `file_object`
  - `upload_session`
  - `upload_chunk`
- 落地了完整的分片上传链路：
  - 初始化上传会话
  - 查询断点续传状态
  - 上传单个分片
  - 合并完成上传
  - 取消上传会话
  - 查询/删除文件对象
- 引入了会话级分布式锁，避免同一上传会话被并发分片写入、重复完成合并或取消时出现竞态。
- 实现了本地存储抽象 `LocalFileStorageService`，并按流式方式合并分片，避免再次把所有分片整块读入 JVM 内存。
- 补上了大文件链路的关键校验：
  - 整文件大小校验
  - 分片大小校验
  - 客户端整文件 hash 校验
  - 可选分片 hash 校验
  - MIME 探测校验
  - 魔数校验
  - 文件 hash 去重复用
- 增加了超时清理任务，对长时间未完成的上传会话自动过期并回收临时分片目录。
- 补充了 `docs/sql/2026-06-24-file-service.sql`，交付新模块建表脚本。
- 补充了 `FileTypeDetectorTest` 与 `LocalFileStorageServiceTest`，覆盖基础文件类型识别与流式分片合并能力。

本次自检重点：
- 当前文件服务已经具备“分片上传 + 断点续传 + 合并校验 + 去重 + 超时清理”的最小可用闭环。
- `micro-teaching-quality` 里的历史 `MultipartFile` 直传接口还没有整体切换到 `fileObjectId` 绑定模式，这会作为后续业务接入层改造继续推进。
- Maven 自动化验证目前受本地沙箱网络限制影响，需要放开依赖下载后再做一次正式构建复核。
