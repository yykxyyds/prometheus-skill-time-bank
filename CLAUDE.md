# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

海南大学课程设计（学年论文），第10组，指导教师：刘德才。

**项目名称**: Prometheus——技能时间银行

以"时间币"为核心的技能互助平台：用户通过提供技能赚取时间币，再用时间币消费他人技能。新人注册赠送100时间币（注意：产品设计文档写20，但代码实现是100）。

- GitHub: <https://github.com/yykxyyds/prometheus-skill-time-bank>
- 进度跟踪: `document/PROGRESS.md`（开发进度、API清单、下一步计划）

## 技术栈

| 层   | 技术                                                                                                                     |
| --- | ---------------------------------------------------------------------------------------------------------------------- |
| 后端  | Spring Boot 3.2 + MyBatis-Plus 3.5.5                                                                                   |
| 数据库 | MySQL 8.0.40，库名 `prometheus_skill_bank`，账户 `root/123456`，端口 3306，安装于 `D:\Dev\config\MySQL\MySQL Server 8.0`，数据目录 `D:\Dev\config\MySQL\MySQL Server 8.0\Data` |
| 构建  | Maven 多模块聚合工程（非微服务），Java 17                                                                                            |
| 前端  | Vue 3 + Vite 8 + Element Plus + Pinia + Axios + ECharts 6 + @iconify/vue                                                  |
| 辅助  | JWT (jjwt 0.12.3)、Hutool 5.8、Lombok、SpringDoc OpenAPI 2.3                                                              |

> ⚠️ **不使用 Spring Cloud / Nacos / 微服务**。项目是 **Spring Boot 多模块聚合工程**，模块间通过 Maven 依赖直接调用 service 接口，不经过 RPC/服务发现。

## 模块架构（已实现）

父项目 `skill-time-bank`，`groupId=com.prometheus`，`version=1.0.2`，7 个子模块：

| 子模块                    | 包路径                      | 职责                                                        | 关键类                                                                                                   |
| ---------------------- | ------------------------ | --------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| `skill-common`         | `com.prometheus.common`  | 统一响应体、全局异常、JWT、BaseEntity、`@RequireAuth` 注解、Jackson 日期格式化 | `Result`, `GlobalExceptionHandler`, `JwtUtil`, `BaseEntity`, `JacksonConfig`                          |
| `skill-user-service`   | `com.prometheus.user`    | 注册/登录/个人信息/关注/文件上传/通知                                        | `UserController`, `UploadController`, `NotificationController`, `User` entity                        |
| `skill-skill-service`  | `com.prometheus.skill`   | 技能广场/发布/分类搜索/需求悬赏                                         | `SkillController`, `BountyController`, `CategoryController`                                           |
| `skill-order-service`  | `com.prometheus.order`   | 订单状态机/订单聊天(HTTP)/私信/时间币冻结                                 | `OrderController`, `ChatController`, `PrivateMessageController`                                       |
| `skill-wallet-service` | `com.prometheus.wallet`  | 钱包余额/时间流水/双盲评价/申诉/公告                                      | `WalletController`, `ReviewController`, `AppealController`, `AnnouncementController`                  |
| `skill-admin-service`  | `com.prometheus.admin`   | **管理员后台**：用户管理、技能审核、申诉处理、公告管理                             | `AdminUserController`, `AdminSkillController`, `AdminAppealController`, `AdminAnnouncementController` |
| `skill-gateway`        | `com.prometheus.gateway` | **统一入口（不是网关）**：Spring Boot 主类 + CORS 配置，聚合启动              | `GatewayApplication`, `CorsConfig`                                                                    |

每个业务模块内部采用 `controller → service → mapper` 分层。module 间通过直接依赖引用（非 RPC）。

### Gateway 说明

`skill-gateway` 不是 Spring Cloud Gateway，只是一个聚合启动模块：

- `@ComponentScan("com.prometheus")` — 扫描所有 7 个模块的 bean
- `@MapperScan(...)` — 扫描所有模块的 MyBatis mapper
- `CorsConfig` — 处理跨域
- 依赖所有 6 个业务模块（common/user/skill/order/wallet/admin）
- **后端入口只有这一个**，端口 8080

## 常用命令

```bash
# 全量编译
cd code/skill-time-bank && mvn clean compile

# 打包（生成 fat JAR，约 36MB）
mvn clean package -DskipTests -pl skill-gateway -am

# 启动后端（必须用 java -jar，不要用 spring-boot:run）
java -jar code/skill-time-bank/skill-gateway/target/skill-gateway-1.0.2.jar

# 编译单个模块
mvn clean compile -pl skill-common -am

# 导入测试数据（init.sql 仅基础数据，需 seed 脚本才有业务演示数据）
MSQL="mysql -u root -p123456 -h 127.0.0.1 -P 3306 prometheus_skill_bank"
$MSQL < code/database/seed_demo.sql

# 启动用户前端（端口 5173，首次需 npm install）
cd code/skill-time-bank-web && npm install && npm run dev

# 启动管理后台（端口 5174，首次需 npm install）
cd code/skill-admin-web && npm install && npm run dev

# Swagger UI
# http://localhost:8080/swagger-ui.html
```

## 数据库

- MySQL 8.0.40，服务名 `MySQL80`，安装于 `D:\Dev\config\MySQL\MySQL Server 8.0`，数据目录 `D:\Dev\config\MySQL\MySQL Server 8.0\Data`，配置文件 `D:\Dev\config\MySQL\MySQL Server 8.0\my.ini`，服务以 `NT AUTHORITY\NetworkService` 身份运行
- C 盘已无 MySQL（Installer 已清理），全部在 D 盘
- 库名: `prometheus_skill_bank`，共 15 张表
- 建表脚本: `code/database/init.sql`
- 初始数据：管理员 admin/admin123、3个测试用户、8 个技能分类
- 测试数据脚本：`code/database/seed_demo.sql`（演示用）、`seed_test_data.sql`（较完整测试数据）。**init.sql 仅有基础数据**，业务表（技能/订单/悬赏/评价等）初始为空，需执行 seed 脚本才能在页面看到交互效果
- 连接URL: `jdbc:mysql://localhost:3306/prometheus_skill_bank?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai`

```bash
# 命令行连接（MySQL 已在系统 PATH）
mysql -u root -p123456 -h 127.0.0.1 -P 3306 prometheus_skill_bank
```

## Docker 部署

课程硬性要求容器化部署。`code/docker-compose.yml` 编排 4 个服务：

| 服务             | 容器名                       | 端口      | 说明                      |
| -------------- | ------------------------- | ------- | ----------------------- |
| mysql          | prometheus-mysql          | 3306    | MySQL 8.0，自动执行 init.sql |
| backend        | prometheus-backend        | 8080    | JRE 17 Alpine，JAR 包     |
| frontend-user  | prometheus-frontend-user  | 5173→80 | Nginx 托管用户端             |
| frontend-admin | prometheus-frontend-admin | 5174→80 | Nginx 托管管理后台            |

```bash
# 启动全部服务（-d 后台）
cd code && docker compose up -d

# 仅重建后端（代码改动后）
docker compose up -d --build backend

# 停止/清理
docker compose down
docker compose down -v   # 同时删除数据卷（重置数据库）
```

> Docker Compose 要求版本 ≥ 3.8，Docker Engine ≥ 20.10

## 前端架构

项目有两个独立前端应用：

### 用户端 `skill-time-bank-web`（端口 5173）

```
skill-time-bank-web/src/
├── api/                # Axios 封装 + 按模块的 API 调用（index.js / user.js / skill.js / message.js / notification.js）
├── stores/user.js      # Pinia store：token, userId, username, role, balance
├── composables/        # 可复用组合式函数（useScrollReveal.js 滚动动画）
├── router/index.js     # 路由，beforeEach 做 auth 守卫
├── views/
│   ├── Home.vue        # 技能广场（首页）
│   ├── Login.vue       # 登录/注册
│   ├── SkillDetail.vue # 技能详情
│   ├── Bounty.vue      # 需求悬赏列表
│   ├── bounty/         # Create.vue（发布悬赏）, Detail.vue（悬赏详情）
│   ├── Messages.vue    # 私信列表（会话列表+消息详情）
│   ├── order/          # OrderList.vue（买方/卖方订单列表）, Detail.vue（订单详情+聊天+评价）
│   └── user/           # Wallet, Profile, MySkills, MyBounties, Appeal（钱包/资料/技能/悬赏/申诉）
├── App.vue
├── main.js
└── style.css
```

### 管理后台 `skill-admin-web`（端口 5174）

独立的管理后台 SPA，从用户端分离出来：

```
skill-admin-web/src/
├── api/index.js        # Axios 封装
├── stores/user.js      # Pinia store（管理员登录态）
├── router/index.js     # 路由，beforeEach 做 admin 守卫
├── views/
│   ├── Login.vue       # 管理员登录
│   ├── Dashboard.vue   # 管理仪表盘
│   └── admin/          # Users.vue（用户管理）, Skills.vue（技能审核）, Appeals.vue（申诉处理）, Announcements.vue（公告管理）, Bounties.vue（悬赏管理）
├── App.vue
├── main.js
└── style.css
```

关键设计：

- **Vite 代理**: 两个前端各自 `/api` → `localhost:8080`，开发环境不需要 CORS
- **Axios 拦截器**: 请求自动带 `Bearer token`，响应自动检查 `code !== 200` 并弹错误提示
- **路由守卫**: 用户端检查登录态，管理后台额外检查 ADMIN 角色
- **登录态**: token + userId + username + role 存 localStorage，Pinia 读取
- **管理后台独立部署**: 普通用户无法访问管理功能，前后端物理隔离

## 关键架构决策

### JWT 认证机制

- 不是 Spring Security 过滤器链，而是自定义 `@RequireAuth` 注解
- `skill-gateway/config/WebMvcConfig` 统一注册 `UserAuthInterceptor`，拦截所有 `/api/**` 请求
- 公开接口用 `@RequireAuth(required = false)` 标记
- 拦截器始终解析 token（有则设 userId，无则跳过），具体强制登录由注解控制
- JWT 工具类在 `skill-common` 的 `JwtUtil`

### 统一响应格式

- 所有 API 返回 `Result` 对象：`{ code: 200, msg: "success", data: ... }`
- 异常通过 `GlobalExceptionHandler` 统一捕获
- 业务异常用 `BusinessException` 抛出

### 实体基类

- `BaseEntity` 定义 `id`（雪花ID）、`createTime`、`updateTime`
- **不是所有表都继承它**——如果表缺少 `update_time` 列，就不要继承

### 订单状态机

- 状态流转: 待确认 → 进行中 → 待确认完成 → 已完成 / 已取消
- 下单时冻结买家时间币，双方确认后解冻并转账

### 双盲评价

- 交易完成后双方互评，写完才能看对方评价
- 7 天自动解盲，双方互评后立即可见
- 信誉雷达图：按时/沟通/专业/态度 四个维度

## 核心 API 清单（已实现）

所有接口前缀 `/api`。详细清单见 `document/PROGRESS.md` 第六节。

**公开接口**（无需登录）:

```
POST /api/user/register    注册（赠送 100 时间币）
POST /api/user/login       登录（返回 JWT token）
GET  /api/user/{userId}/profile  查看他人主页
GET  /api/skill/list       技能广场（分页+分类+搜索+排序）
GET  /api/skill/{id}       技能详情
GET  /api/category/list    技能分类列表
GET  /api/bounty/list      悬赏列表
GET  /api/review/user/{userId}      评价列表
GET  /api/review/reputation/{userId} 信誉雷达数据
GET  /api/announcement/list 公告列表
GET  /api/announcement/{id} 公告详情
```

**需登录**（Header: `Authorization: Bearer <token>`）:

- 用户: `GET/PUT /api/user/profile`
- 技能: `POST/PUT /api/skill`, `PUT /api/skill/{id}/offline`, `GET /api/skill/my`
- 悬赏: `POST /api/bounty`, `POST /api/bounty/{id}/apply`, `PUT /api/bounty/{id}/accept|reject|complete`
- 订单: `POST /api/order`, `PUT /api/order/{id}/confirm|buyer-complete|seller-complete|cancel`, `GET /api/order/{id}|/buyer|/seller`
- 聊天: `GET/POST /api/chat/order/{orderId}`
- 私信: `POST /api/chat/private/send`, `GET /api/chat/private/conversations|/messages/{userId}|/unread`, `PUT /api/chat/private/read/{userId}`
- 通知: `GET /api/notification/list`, `GET /api/notification/unread-count`, `PUT /api/notification/{id}/read`
- 钱包: `GET /api/wallet/balance`, `GET /api/wallet/transactions`
- 评价: `POST /api/review`
- 申诉: `POST /api/appeal`

**管理员接口**（需 ADMIN 角色）:

```
GET  /api/admin/users                 用户列表
PUT  /api/admin/users/{id}/status     启用/禁用用户 {status: 0|1}
GET  /api/admin/skill/list            技能审核列表
PUT  /api/admin/skill/{id}/status     技能审核（通过/拒绝）{status: 1|3}
GET  /api/appeal/list                 申诉列表
PUT  /api/appeal/{id}/handle          处理申诉
POST /api/announcement                发布公告
PUT  /api/announcement                编辑公告
DELETE /api/announcement/{id}         删除公告
```

**文件上传**（需登录）:

```
POST /api/upload/avatar               上传头像（multipart, ≤5MB, 仅图片）
```

## 已知问题与注意事项

见 CLAUDE.md 排坑记录（以下为摘要，完整版保留在下方排坑记录节）：

1. MySQL JDBC 字符编码必须用 `UTF-8`（不是 `utf8mb4`）
2. `spring-boot-maven-plugin` 只在 gateway 子模块激活，父 POM 只放入 `pluginManagement`
3. 各模块 `WebMvcConfig` 必须用不同 Bean 名（`@Configuration("xxxWebMvcConfig")`）
4. `@RequestParam` 必须显式写 `name` 属性
5. 前端 `style.css` 须清理 Vite 模板预设样式
6. 不要用 `&&` 链关键构建命令
7. 文件上传/图片访问不能用相对路径 `file:uploads/`，必须用绝对路径配置 `app.upload-dir`

## 课程硬性要求

| 要求    | 说明                                                  |
| ----- | --------------------------------------------------- |
| 团队规模  | 3-6人/组，每组设组长1名                                      |
| 架构    | 分层架构（父子项目/聚合项目），前后端分离                               |
| AI 融入 | 必须融入 AI 技术或使用 AI 工具辅助开发                             |
| 数据库   | 至少10张表（已实现15张），遵循范式设计，绘制ER图                         |
| 代码规范  | 符合《阿里巴巴 Java 开发手册》\[强制]部分                           |
| 部署    | Docker / Kubernetes 容器化                             |
| 版本管理  | Git（GitHub）                                         |
| 交付物   | 6项：任务书/SRS/计划与进度/详细设计/答辩PPT/代码+SQL+README |

## 目录结构

```
├── document/                      # 所有文档和交付物
│   ├── submission/                # ★ 最终提交的 5 份交付物（含临时文件）
│   │   ├── 最终项目-1-项目任务书-第10组.doc          # 任务书（旧格式 .doc）
│   │   ├── 最终项目-2-需求规格说明-第10组.docx        # SRS（含目录+图表，~937KB）
│   │   ├── 最终项目-3-项目计划与进度比较-第10组.xlsx   # Excel 计划表
│   │   ├── 最终项目-4-项目文档-详细设计-第10组.docx    # 详细设计（含架构图/ER图/类图/流程图）
│   │   ├── 最终项目-5-答辩PPT-第10组.pptx            # 答辩演示文稿
│   │   ├── _temp_output.docx                        # docx 生成中间临时文件，可忽略
│   │   └── ~$项目-*-第10组.docx                      # Word 锁定文件（打开文档时自动生成）
│   │
│   ├── demo/                        # 往届第1组完整提交（★ 格式/内容参考模板）
│   │   ├── 最终项目-1-项目任务书-小组1.doc
│   │   ├── 最终项目-2-需求规格说明-小组1.docx
│   │   ├── 最终项目-3-项目计划与进度比较-小组1.xlsx
│   │   ├── 最终项目-4-项目文档-详细设计-小组1.docx
│   │   ├── 最终项目-5-答辩PPT-李卓-小组1.pptx
│   │   └── 项目代码+建表SQL+自动化脚本+README文档/     # 仅占位说明文件
│   │
│   ├── 总体要求.pdf                  # 课程 8 项交付物要求原文
│   ├── PPT模板参考.pptx              # 学院 PPT 模板（自带校徽/配色）
│   ├── PROGRESS.md                  # 开发进度、API 清单、已知问题、下一步计划
│   ├── 产品构思-头脑风暴.md           # 产品定位（时间币逻辑、用户场景、视觉方向）
│   ├── Maven多模块与分层架构.md       # 多模块 Maven 工程 + 分层架构讲义
│   ├── Claude Code高效使用建议.md     # 本项目的 Claude Code 协作最佳实践
│   ├── 多Agent开发策略.md            # Claude Code Agent 并行开发策略
│   ├── 小组成员消息.txt              # 4 位团队成员姓名及分工定位
│   ├── todo补充.txt                 # 功能迭代清单（已全部✔）+ PPT/SRS/文档修改意见
│   │
│   ├── screenshots/                 # 前端页面截图 + 文档插图
│   │   ├── *.png                    # 11 张页面截图：首页/技能详情/钱包/个人中心/消息/
│   │   │                            #   订单(买方)/管理后台(用户/技能/申诉/公告)
│   │   ├── diagrams/                # 文档用图：architecture / er_diagram /
│   │   │                            #   class_diagram / flow_chart（由 gen_diagrams_v3.py 生成）
│   │   └── ref/                     # PPT 用参考截图 7 张（img_0~img_6）
│   │
│   ├── gen_diagrams_v3.py           # matplotlib 生成 4 张文档插图（架构/ER/类/流程）
│   ├── generate_ppt.js              # pptxgenjs 生成 PPT 脚本（v1）
│   ├── generate_ppt_v2.js           # pptxgenjs 生成 PPT 脚本（v2 改进版）
│   └── screenshot.py                # Playwright 自动截取前端页面（5173/5174 端口）
│
├── code/                         # 所有项目代码
│   ├── database/init.sql            # 15张表 DDL + 初始数据
│   ├── skill-time-bank/             # 后端 Maven 多模块工程（7 子模块）
│   ├── skill-time-bank-web/         # 用户端 Vue 3 工程
│   └── skill-admin-web/             # 管理后台 Vue 3 工程
├── .claude/                         # Claude Code 配置（settings.json: bypassPermissions）
└── CLAUDE.md
```

### document/ 文件关系速查

| 需求 | 文件 |
|------|------|
| 搞清楚课程要求 | `总体要求.pdf` |
| 格式参考（文档怎么写） | `demo/最终项目-*-小组1.*` — 第1组的完整提交 |
| PPT 模板 | `PPT模板参考.pptx` — 学院统一模板 |
| 项目进度 / API 清单 | `PROGRESS.md` |
| 产品定位、设计思路 | `产品构思-头脑风暴.md` |
| 架构说明 | `Maven多模块与分层架构.md` |
| 团队分工 | `小组成员消息.txt` |
| 待办 / 修改意见 | `todo补充.txt` |
| 生成文档插图（架构/ER/类/流程图） | 运行 `python document/gen_diagrams_v3.py` → 输出到 `screenshots/diagrams/` |
| 截取前端页面 | 启动前后端 → 运行 `python document/screenshot.py` → 输出到 `screenshots/` |
| 生成 PPT | 运行 `node document/generate_ppt_v2.js` |
| 当前提交的 5 份交付物 | `submission/最终项目-[1-5]-第10组.*` |

## 交付物清单

|  #  | 交付物                 | 格式      |  状态 |
| :-: | ------------------- | ------- | :-: |
|  1  | 项目任务书               | `.doc`  |  ✅  |
|  2  | 需求规格说明 (SRS)        | `.docx` |  ✅  |
|  3  | 项目计划与进度比较           | `.xlsx` |  ✅  |
|  4  | 详细设计文档（含ER图）        | `.docx` |  ✅  |
|  5  | 答辩PPT               | `.pptx` |  ✅  |
|  6  | 项目代码 + SQL + README | 代码/文档   |  ✅  |

## 自检验证规范

代码改动后必须验证，不要写完就说"完成"。

- 后端: `mvn clean compile` → `java -jar ...` → curl 测试
- 前端: `npm run dev` → 浏览器检查
- SQL: 连接 MySQL 执行 `SHOW TABLES;` / `DESC table_name;`
- 验证三种输入: 正常 + 异常（空值/越界） + 边界

***

## 排坑记录（开发中遇到的已解决问题）

### 1. MySQL JDBC 字符编码

**问题**：`application.yml` 中 `characterEncoding=utf8mb4` 导致 `java.sql.SQLException: Unsupported character encoding 'utf8mb4'`

**原因**：MySQL JDBC 驱动使用 Java 字符集名，不是 MySQL 字符集名。

**解决**：`characterEncoding=UTF-8`（注意是 UTF-8 不是 utf8mb4）。若需 emoji 支持，用 `connectionCollation=utf8mb4_unicode_ci` 参数，数据库层面设置 `utf8mb4`。

### 2. Spring Boot Maven Plugin 覆盖父 POM 的 repackage 绑定

**问题**：`mvn package` 后 skill-gateway 的 JAR 只有 4KB，不包含依赖；`spring-boot:run` 运行时 wallet/announcement 等模块的 Controller 未被加载，报 `NoResourceFoundException`。

**原因**：父 POM（`skill-time-bank`）的 `<build><plugins>` 中声明了 `spring-boot-maven-plugin`，这会**覆盖** Spring Boot 父 POM（`spring-boot-starter-parent`）中该插件的默认配置，导致 `repackage` goal 失去与 `package` 生命周期的绑定。

**解决**：

- 父 POM 中只将 `spring-boot-maven-plugin` 放入 `<pluginManagement>`（提供配置模板，不激活）
- 仅在 gateway 子模块的 `<build><plugins>` 中声明并添加 `<executions><execution><goals><goal>repackage</goal>` 激活 repackage
- **不要用** **`mvn spring-boot:run`**，它解析多模块反应器依赖不可靠；始终用 `mvn clean package -pl skill-gateway -am` + `java -jar`。

### 3. WebMvcConfig Bean 名称冲突

**问题**：应用启动报 `ConflictingBeanDefinitionException: Annotation-specified bean name 'webMvcConfig' for bean class [com.prometheus.order.config.WebMvcConfig] conflicts with ... [com.prometheus.user.config.WebMvcConfig]`

**原因**：三个模块（user / order / wallet）各自有 `WebMvcConfig` 类，Spring 默认用类名（首字母小写）作为 Bean 名，三个同名类冲突。

**解决**：每个模块的 WebMvcConfig 加唯一值：`@Configuration("userWebMvcConfig")`、`@Configuration("orderWebMvcConfig")`、`@Configuration("walletWebMvcConfig")`。

### 4. 实体类 extends BaseEntity 与数据库表字段不一致

**问题**：`skill_category` 表的 `SkillCategory` 实体继承 `BaseEntity`，但数据库中该表只有 `create_time` 没有 `update_time`，查询时报 `Unknown column 'update_time'`。

**解决**：修改数据库表补齐缺失字段：`ALTER TABLE skill_category ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;`

**教训**：建表 SQL 需要和实体类同步检查。`BaseEntity` 定义了 `id`、`createTime`、`updateTime`，所有继承它的实体对应的表必须有这三列。如果某张表不需要 `update_time` 或有特殊的 id 策略（如 AUTO），就不要继承 `BaseEntity`。

### 5. @RequestParam 参数名丢失

**问题**：Controller 方法中 `@RequestParam(defaultValue = "1") int page` 不写 `name="page"` 时，Spring 报 `IllegalArgumentException: Name for argument of type [int] not specified`。

**原因**：Java 编译器默认不保留方法参数名，Spring 需要靠注解的 `name`/`value` 属性获知参数名。

**解决**：所有 `@RequestParam` 必须显式写 `name` 属性：`@RequestParam(name = "page", defaultValue = "1") int page`。

**备注**：父 POM 的 `maven-compiler-plugin` 已配置 `<arg>-parameters</arg>`（Java 17 保留参数名），但显式写 `name` 仍是最佳实践，不依赖编译器行为。

### 6. Vite 模板残留样式冲突

**问题**：前端页面渲染异常，`Vite` 初始化模板生成的 `style.css` 包含 `#app` 选择器（固定宽度 1126px / `text-align:center` / 边框等），与 Vue 组件自身的 `App.vue` 样式冲突。

**解决**：删除 `style.css` 中的模板预设样式，只保留 `* { margin:0; padding:0; box-sizing:border-box; }` + `body { font-family }` 等最小全局重置。

### 7. `mvn` 命令链注意 `&&` 短路

**问题**：`cmd //c "taskkill ..." | awk ... && cd ... && mvn clean compile` 中，`awk` 的退出码（128）导致 `&&` 短路，编译未执行。

**解决**：每个独立命令分开调用，不要用 `&&` 串联关键构建命令。kill → compile → start 各自独立执行。

### 8. 文件上传/图片访问不能用相对路径

**问题**：`WebMvcConfig.java` 和 `UploadController.java` 都用 `file:uploads/` 相对路径。`java -jar` 运行时 JVM 工作目录不确定（可能不是项目目录），导致图片返回 404、上传文件存到错误位置。前端所有头像/封面图都不显示。

**原因**：相对路径依赖于 JVM 进程的 working directory，不同启动方式（IDE、java -jar、Docker）下 working directory 不同，结果不一致。

**解决**：

- `application.yml` 新增 `app.upload-dir` 配置项，指向绝对路径
- `WebMvcConfig.java`：`@Value("${app.upload-dir}")` 注入后用 `"file:" + uploadDir + "/"` 
- `UploadController.java`：`Paths.get(this.uploadDir, subDir)` 代替 `Paths.get("uploads", subDir)`
- **Docker 部署时**需要修改 `app.upload-dir` 为容器内路径（如 `/app/uploads`），且 Dockerfile 需 `COPY` 种子图片
