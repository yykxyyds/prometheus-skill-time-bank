# Prometheus——技能时间银行

> 海南大学课程设计 · 第10组 · 指导教师：刘德才

一个以"时间币"为核心的技能互助平台。用户通过提供技能赚取时间币，再用时间币消费他人技能。

## 技术栈

| 层   | 技术                                               |
| --- | ------------------------------------------------ |
| 后端  | Spring Boot 3.2 + MyBatis-Plus 3.5.5 + Maven 多模块 |
| 数据库 | MySQL 8.0                                        |
| 前端  | Vue 3 + Vite + Element Plus + Pinia + Axios      |
| 认证  | JWT (jjwt 0.12.3)                                |
| 工具  | Hutool 5.8、Lombok、SpringDoc OpenAPI 2.3          |

## 拉取项目

> 仓库为私有，需先接受 GitHub 邀请（检查邮箱或 <https://github.com/yykxyyds/prometheus-skill-time-bank/invitations）>

```bash
cd "专门放项目代码的PATH"
git clone git@github.com:yykxyyds/prometheus-skill-time-bank.git
cd prometheus-skill-time-bank/code
```

> 没配置 SSH Key 的话用 HTTPS：`git clone https://github.com/yykxyyds/prometheus-skill-time-bank.git`

拉取后可以直接让 Claude Code 帮你把代码跑起来，下面都是手动操作方法。

## 环境要求

- **JDK 17** 或以上
- **Maven 3.8** 或以上
- **Node.js 18** 或以上
- **MySQL 8.0** 或以上（已有建表脚本）

## 数据库初始化

1. 启动 MySQL 服务
2. 确认 `application.yml` 中数据库密码与本地一致：
   - 文件位置：`skill-time-bank/skill-gateway/src/main/resources/application.yml`
   - 默认 `username: root`，`password: root`，按实际情况修改
3. 执行建表脚本：

```bash
mysql -u root -p < database/init.sql
```

> 脚本创建 `prometheus_skill_bank` 数据库、15 张表，并插入初始数据（管理员 + 测试用户 + 8 个技能分类）。

1. （可选）导入测试数据，让前端有内容可看：

```bash
mysql -u root -p prometheus_skill_bank < database/seed_test_data.sql
```

## 后端启动

```bash
# 1. 进入后端项目
cd skill-time-bank

# 2. 编译
mvn clean compile

# 3. 打包（生成 fat JAR）
mvn clean package -DskipTests -pl skill-gateway -am

# 4. 启动（端口 8080）
java -jar skill-gateway/target/skill-gateway-1.0.2.jar
```

> ⚠️ 不要用 `mvn spring-boot:run`，必须用 `java -jar`。

## 前端启动

```bash
# 1. 进入前端项目
cd skill-time-bank-web

# 2. 安装依赖（首次运行）
npm install

# 3. 启动开发服务器（端口 5173）
npm run dev
```

浏览器访问 `http://localhost:5173`，前端自动将 `/api` 请求代理到后端 `localhost:8080`。

## Docker 一键部署

> 无需安装 JDK/Node/MySQL，只需 Docker Desktop。适合答辩演示和快速交付。

### 前置准备

首次部署需要先打好后端 JAR 包（Dockerfile 直接复制 JAR，不在容器内编译）：

```bash
cd code/skill-time-bank
mvn clean package -DskipTests -pl skill-gateway -am
cd ..
```

### 国内网络配置代理（可选）

如果 Docker 拉取镜像超时（如 `dial tcp ... connectex`），需为 Docker Daemon 配置代理。

**1. 确认 Clash Verge 已运行**（端口 7897 处于 LISTENING 状态）：

```bash
netstat -ano | grep 7897
```

**2. 打开 Docker Desktop → Settings → Resources → Proxies**，填入：

```
HTTP Proxy:  http://host.docker.internal:7897
HTTPS Proxy: http://host.docker.internal:7897
```

> 不能用 `127.0.0.1:7897`——Docker Daemon 跑在 WSL2 虚拟机里，127.0.0.1 指向 VM 自身而非宿主机。

**3. 点击 Apply & Restart**，等待 Docker Engine 重新启动。

### 停止本机服务（避免端口冲突）

Docker 容器会占用 8080/5173/5174 端口，如果本机已启动后端或前端 dev server，先停掉：

```bash
# 停止本机 MySQL（可选——Docker MySQL 映射 3307，不冲突）
net stop MySQL80

# 停止后端和前端 dev server
taskkill /f /im java.exe 2>nul
# 关掉 Vite dev server（终端 Ctrl+C 或 taskkill /f /im node.exe）
```

### 一键启动

```bash
cd code
docker compose up -d
```

> 首次会拉取基础镜像（mysql:8.0 / nginx:alpine / node:20-alpine / eclipse-temurin:17-jre-alpine）+ 构建 3 个自定义镜像，约 5-10 分钟。之后秒启动。

### 等待就绪 + 导入演示数据

```bash
# 等待 MySQL 和 Backend 变为 healthy（约 30-60 秒）
docker compose ps

# 导入演示数据（16 用户 + 30 技能 + 15 订单 + 8 悬赏）
docker exec -i prometheus-mysql mysql -uroot -proot prometheus_skill_bank < database/seed_demo.sql
```

### 访问

| 服务      | 地址                                      | 容器端口        |
| ------- | --------------------------------------- | ----------- |
| 用户端     | <http://localhost:5173>                 | 80 (nginx)  |
| 管理后台    | <http://localhost:5174>                 | 80 (nginx)  |
| 后端 API  | <http://localhost:8080>                 | 8080 (Java) |
| Swagger | <http://localhost:8080/swagger-ui.html> | —           |
| MySQL   | `localhost:3307`, root/root             | 3306        |

### 常用 Docker 命令

```bash
# 状态 & 日志
docker compose ps                          # 查看所有容器状态
docker compose logs -f backend             # 实时查看后端日志
docker compose top                         # 查看各容器进程

# 重启/重建
docker compose restart backend             # 重启后端（仅 JAR 改动时，无需重建镜像）
docker compose up -d --build backend       # 改后端代码后重建镜像
docker compose up -d --build frontend-user # 改前端代码后重建镜像

# 停止/清理
docker compose down                        # 停止全部容器，保留数据卷
docker compose down -v                     # 停止 + 删除数据卷（重置数据库）

# 进入容器
docker exec -it prometheus-mysql mysql -uroot -proot              # MySQL 命令行
docker exec -it prometheus-backend sh                              # 后端容器 shell
```

> **改代码后**：后端 `mvn package` → `docker compose up -d --build backend`；前端 `docker compose up -d --build frontend-user`。容器端口 80 映射到宿主机 5173/5174 保持不变。

## 默认账户

| 角色   | 用户名        | 密码         |
| ---- | ---------- | ---------- |
| 管理员  | `admin`    | `admin123` |
| 测试用户 | `testuser` | `123456`   |
| 测试用户 | `test99`   | `123456`   |
| 测试用户 | `yykxyyds` | `123456`   |

## 项目结构

```
code/
├── docker-compose.yml            # Docker 编排（4 服务）
├── database/
│   ├── init.sql                  # 15张表 DDL + 初始数据
│   └── seed_demo.sql             # 演示数据种子
├── skill-time-bank/              # 后端 Maven 多模块工程
│   ├── Dockerfile                #   后端镜像（JRE 17 Alpine）
│   ├── skill-common/             #   公共模块：Result、异常、JWT、BaseEntity
│   ├── skill-user-service/       #   用户模块：注册/登录/个人信息
│   ├── skill-skill-service/      #   技能模块：技能广场/分类/悬赏
│   ├── skill-order-service/      #   订单模块：订单状态机/聊天/私信
│   ├── skill-wallet-service/     #   钱包模块：余额/评价/申诉/公告
│   ├── skill-admin-service/      #   管理模块：用户/技能/申诉管理
│   └── skill-gateway/            #   聚合启动模块（统一入口，端口8080）
├── skill-time-bank-web/          # 用户前端 Vue 3
│   ├── Dockerfile                #   两阶段构建（Node → Nginx）
│   ├── nginx.conf                #   Nginx SPA + /api 反向代理
│   └── src/
│       ├── api/                  #   Axios 封装 + 接口调用
│       ├── stores/               #   Pinia 状态管理
│       ├── router/               #   路由 + 守卫
│       └── views/                #   页面组件
├── skill-admin-web/              # 管理后台 Vue 3
│   ├── Dockerfile                #   两阶段构建（同用户端）
│   ├── nginx.conf                #   Nginx SPA + /api 反向代理
│   └── src/
└── README.md
```

## 核心功能

- **技能广场** — 首页即技能信息流，分类搜索，无需登录浏览
- **需求悬赏** — 发布需要的技能，悬赏时间币
- **订单交易** — 完整的技能交易流程（待确认→进行中→已完成）
- **时间银行** — 钱包余额、冻结资金、收支流水
- **双盲评价** — 交易后互评，写完才能看对方评价，信誉雷达图
- **申诉公告** — 纠纷处理和平台公告

## API 概览

所有接口前缀 `/api`，统一返回格式 `{ code: 200, msg: "success", data: ... }`。

需登录的接口在 Header 中携带 `Authorization: Bearer <token>`。

| 接口                        | 说明          |  认证 |
| ------------------------- | ----------- | :-: |
| `POST /api/user/register` | 注册          |  -  |
| `POST /api/user/login`    | 登录，返回 JWT   |  -  |
| `GET /api/skill/list`     | 技能广场（分页+搜索） |  -  |
| `GET /api/skill/{id}`     | 技能详情        |  -  |
| `GET /api/category/list`  | 分类列表        |  -  |
| `GET /api/bounty/list`    | 悬赏列表        |  -  |
| `POST /api/skill`         | 发布技能        |  登录 |
| `POST /api/order`         | 下单          |  登录 |
| `GET /api/wallet/balance` | 查看余额        |  登录 |
| `GET /api/admin/users`    | 用户管理        | 管理员 |

完整 API 清单见项目根目录 `PROGRESS.md` 第六节。

## 拉取最新代码

```bash
git pull origin master
```

如果本地有改动还没 commit，先暂存：

```bash
git stash
git pull origin master
git stash pop
```

## 常用命令速查

```bash
# === 本地开发 ===
# 后端编译
cd code/skill-time-bank && mvn clean compile

# 后端打包
cd code/skill-time-bank && mvn clean package -DskipTests -pl skill-gateway -am

# 后端运行
java -jar code/skill-time-bank/skill-gateway/target/skill-gateway-1.0.2.jar

# 前端运行
cd code/skill-time-bank-web && npm run dev

# 管理后台运行
cd code/skill-admin-web && npm run dev

# 数据库连接（本机 MySQL）
"D:/MySQL/mysql-8.0.45-winx64/bin/mysql.exe" -u root -proot -h 127.0.0.1 -P 3306 prometheus_skill_bank

# === Docker 部署 ===
# 一键启动
cd code && docker compose up -d

# 代码改动后重建
docker compose up -d --build backend       # 改后端
docker compose up -d --build frontend-user # 改用户端

# 导入演示数据
docker exec -i prometheus-mysql mysql -uroot -proot prometheus_skill_bank < database/seed_demo.sql

# 查看日志
docker compose logs -f backend

# 停止
docker compose down
```

## 团队

| 角色 | 姓名 |
| -- | -- |
| 组长 | —  |
| 组员 | —  |
| 组员 | —  |
| 组员 | —  |

> 请各成员在此填入姓名

