# Prometheus 技能时间银行 — 开发进度

> 更新时间：2026-05-05（最新刷新）  
> 项目地址：https://github.com/yykxyyds/prometheus-skill-time-bank  
> 当前状态：后端 6 模块全部编译通过，前端 10 个页面完成（19 文件共 +3700 行），数据库有基础数据但业务表为空

---

## 一、后端开发

### 1.1 数据库 ✅ 已完成

14 张表 DDL + 初始数据（管理员 admin/admin123、3 测试用户、8 个分类）
- 建表脚本: `项目代码/database/init.sql`
- 库名: `prometheus_skill_bank`，MySQL 8.0.45，数据目录 `D:\MySQL\data`
- ⚠️ **init.sql 仅插入用户和分类，业务表（skill/bounty/order/review 等）初始为空**。需手动插入测试数据才能在前端看到效果
- 当前数据量：user=4, skill_category=8, time_transaction=1, 其余表=0

### 1.2 Maven 多模块工程 ✅ 已完成

| 模块 | 文件数 | 编译 | API 测试 |
|------|:------:|:----:|:--------:|
| skill-common | 8 | ✅ | — |
| skill-user-service | 19 | ✅ | ✅ register/login/profile |
| skill-skill-service | 16 | ✅ | ✅ list/detail/category |
| skill-order-service | 10 | ✅ | ✅ CRUD/confirm |
| skill-wallet-service | 20 | ✅ | ✅ balance/transactions/review/appeal/announcement |
| skill-gateway | 2 | ✅ | — |

- ✅ 全部编译通过，`mvn clean package -DskipTests` 生成约 36MB fat JAR
- ✅ 9 个核心 API 端点自检通过

### 1.3 功能实现状态

| 功能 | 状态 | 备注 |
|------|:----:|------|
| 用户注册/登录 | ✅ | JWT + BCrypt，注册赠送 100 时间币 |
| 个人资料查看/修改 | ✅ | 自己需登录，他人公开 |
| 技能广场（分页+搜索） | ✅ | 分类筛选 / 关键词搜索 |
| 技能发布/下架/更新 | ✅ | 需登录，只能操作自己的 |
| 需求悬赏 CRUD | ✅ | 发布/申请/接受/拒绝/完成 |
| 订单状态机 | ✅ | 待确认→进行中→待确认完成→已完成/已取消 |
| 时间币冻结/解冻 | ✅ | 下单冻结 → 双方确认后转账 |
| 钱包余额/流水 | ✅ | 余额 + 冻结 + 收支明细 |
| 双盲评价 | ✅ | 7 天自动解盲 / 双方互评立即可见 |
| 信誉雷达图 | ✅ | 按时/沟通/专业/态度 四维 |
| 申诉系统 | ✅ | 提交 → 管理员处理 |
| 公告管理 | ✅ | 管理员 CRUD，公开列表 |
| 订单聊天 | ✅ | HTTP 接口（非 WebSocket） |
| 通知 | ⚠️ | 后端有接口，前端未对接 |
| JWT 认证 | ✅ | `@RequireAuth` 自定义注解 + 拦截器 |

---

## 二、前端开发

### 2.1 基础设施 ✅ 已完成

- ✅ Vue 3 + Vite 8 + Element Plus + Pinia + Axios + ECharts
- ✅ 11 条路由，含 auth + admin 守卫
- ✅ Axios 封装（JWT 自动注入 + 错误拦截）
- ✅ Vite 代理 `/api` → `localhost:8080`
- ✅ 全局样式（暖色调橙红/金色主题）
- ✅ 玻璃拟态导航栏（sticky header + 毛玻璃效果）
- ✅ Element Plus 全局主题覆盖
- ✅ 页面切换动画（fade + slide）

### 2.2 页面完成情况

| 页面 | 路由 | 状态 | 说明 |
|------|------|:----:|------|
| 技能广场（首页） | `/` | ✅ | Hero 区 + 搜索栏 + 分类筛选 + 卡片网格 + 分页，完整交互 |
| 登录/注册 | `/login` | ✅ | 左右分栏布局，表单验证，登录/注册切换，loading 态 |
| 技能详情 | `/skill/:id` | ✅ | 面包屑 + 技能信息 + 下单功能，数量选择 |
| 需求悬赏 | `/bounty` | ✅ | 列表展示 + 状态标签 + 搜索 |
| 时间银行 | `/wallet` | ✅ | 资产卡片 + ECharts 雷达图 + 时间流水列表，需登录 |
| 个人中心 | `/profile` | ✅ | 个人信息卡片 + 编辑资料弹窗，自己/他人双模式 |
| 我的技能 | `/my-skills` | ✅ | 技能列表 + 新建/编辑弹窗 + 下架操作，需登录 |
| 管理-用户 | `/admin/users` | ✅ | 用户列表 + 启用/禁用，需 ADMIN |
| 管理-申诉 | `/admin/appeals` | ✅ | 申诉列表 + 状态标签 + 处理弹窗，需 ADMIN |
| 管理-公告 | `/admin/announcements` | ✅ | 公告列表 + 新建/编辑/删除，需 ADMIN |

### 2.3 缺失页面（前端已规划路由但未实现）

| 页面 | 说明 | 优先级 |
|------|------|:------:|
| 订单列表/详情 | 后端 API 已有（buyer/seller），前端缺页面 | 高 |
| 订单聊天 | 后端 HTTP 接口已就绪，前端缺聊天 UI | 中 |
| 通知中心 | 后端接口已有，前端未对接 | 低 |

---

## 三、文档交付物（8 项）

| # | 交付物 | 状态 | 优先级 |
|:--:|--------|:----:|:------:|
| 1 | 项目任务书 (.doc) | ❌ 未开始 | 高 |
| 2 | 需求规格说明 SRS (.docx) | ❌ 未开始 | 高 |
| 3 | 项目计划与进度比较 (.xlsx) | ❌ 未开始 | 中 |
| 4 | 详细设计文档 (.docx) | ❌ 未开始 | 高 |
| 5 | 答辩 PPT (.pptx) | ❌ 未开始 | 高 |
| 6 | 演示视频 | ❌ 未开始 | 中 |
| 7 | 部署说明文档 | ❌ 未开始 | 中 |
| 8 | 项目代码 + SQL + README | ⚠️ 缺 README | 中 |

---

## 四、加分项（可选）

| 加分项 | 状态 | 说明 |
|--------|:----:|------|
| 安全认证 JWT | ✅ 已实现 | `@RequireAuth` 注解 + 拦截器，需文档说明 |
| 设计模式 | ⚠️ | 订单状态机（策略模式雏形），可专门写说明 |
| Swagger 接口文档 | ⚠️ | springdoc 依赖已引入，需给 Controller 加注解 |
| Docker 部署 | ❌ | 需写 Dockerfile + docker-compose.yml |
| Redis 缓存 | ❌ | 可缓存技能广场热门列表 |
| 幂等设计 | ❌ | 订单支付接口可加唯一请求号 |
| 单元测试 | ❌ | 后端零测试 |
| CI/CD | ❌ | 未配置 |

---

## 五、下一步建议（按优先级）

### 高优先级 🔴

1. **插入测试数据** — 技能/悬赏/订单/评价等业务表均为空，前端页面无法展示效果，需执行测试数据 SQL
2. **前端：订单管理页面** — 后端 API 齐全，前端缺"买方订单"和"卖方订单"两个列表页 + 订单详情页，这是核心交易闭环
3. **写详细设计文档** — 含 ER 图、模块结构、接口列表，代码已完成，写起来很快
4. **写需求规格说明 SRS** — 用例图、功能需求、非功能需求
5. **项目任务书** — 格式参考 `提交示例/`
6. **答辩 PPT** — 项目背景 + 技术架构 + 亮点展示

### 中优先级 🟡

6. **前端：订单聊天页** — HTTP 聊天接口已有，做简单消息列表即可
7. **Docker 部署** — Dockerfile + docker-compose（MySQL + 后端 + 前端）
8. **Swagger 接口文档** — 给 Controller 加注解，访问 `/swagger-ui.html`
9. **README 文档** — 项目说明 + 快速开始 + 技术栈

### 低优先级 🟢

10. **前端：通知中心** — 对接已有通知接口
11. **Redis 缓存** — 提升技能广场性能
12. **单元测试** — Service 层关键逻辑
13. **录制演示视频** — 功能稳定后再录

---

## 六、后端 API 清单（供前后端联调参考）

### 公开接口（无需登录）

```
GET    /api/skill/list?page=&size=&categoryId=&keyword=&sort=
GET    /api/skill/{id}
GET    /api/category/list
GET    /api/bounty/list?page=&size=&status=
GET    /api/review/user/{userId}
GET    /api/review/reputation/{userId}
GET    /api/announcement/list
GET    /api/announcement/{id}
POST   /api/user/register  {username, password, email}
POST   /api/user/login     {username, password}  → {token, userId, username, role, balance}
GET    /api/user/{userId}/profile
```

### 需登录（Header: `Authorization: Bearer <token>`）

```
# 用户
GET    /api/user/profile
PUT    /api/user/profile    {email, phone, bio}

# 技能
POST   /api/skill           {title, description, price, categoryId}
PUT    /api/skill           {id, title, description, price, categoryId}
PUT    /api/skill/{id}/offline
GET    /api/skill/my

# 悬赏
POST   /api/bounty          {title, description, reward, deadline}
POST   /api/bounty/{id}/apply  {message}
PUT    /api/bounty/{id}/accept/{applicationId}
PUT    /api/bounty/{id}/reject/{applicationId}
PUT    /api/bounty/{id}/complete

# 订单
POST   /api/order           {sellerId, skillId, amount}
PUT    /api/order/{id}/confirm
PUT    /api/order/{id}/buyer-complete
PUT    /api/order/{id}/seller-complete
PUT    /api/order/{id}/cancel
GET    /api/order/{id}
GET    /api/order/buyer
GET    /api/order/seller

# 聊天
GET    /api/chat/order/{orderId}
POST   /api/chat/order/{orderId}  {content}

# 钱包
GET    /api/wallet/balance
GET    /api/wallet/transactions?page=&size=

# 评价
POST   /api/review          {orderId, targetId, score, comment, punctualityScore, communicationScore, professionalScore, attitudeScore}

# 申诉
POST   /api/appeal          {orderId, reason, evidence}
```

### 管理员接口（需 ADMIN 角色）

```
GET    /api/admin/users
PUT    /api/admin/users/{id}/status   {status: 0|1}
GET    /api/appeal/list?page=&size=&status=
PUT    /api/appeal/{id}/handle        {result}
POST   /api/announcement              {title, content, isTop}
PUT    /api/announcement              {id, title, content, isTop}
DELETE /api/announcement/{id}
```
