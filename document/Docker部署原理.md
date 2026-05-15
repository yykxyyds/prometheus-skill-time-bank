# Docker 部署原理与实战

## 一、Docker 核心概念（30 秒理解）

用**集装箱**类比最好理解：

| 概念 | 类比 | 说明 |
|------|------|------|
| **镜像 (Image)** | 集装箱的**设计图纸** | 定义运行环境：装什么系统、装什么软件、复制哪些文件、执行什么命令。只读模板 |
| **容器 (Container)** | 按图纸造出来的**真实集装箱** | 镜像的运行实例。有独立的文件系统、网络、进程空间，与宿主机隔离 |
| **Docker Compose** | 多艘货轮的**调度中心** | 用一份 YAML 编排多个容器：谁先启动、怎么通信、端口怎么映射、数据怎么持久化 |
| **数据卷 (Volume)** | 集装箱外挂的**储物柜** | 容器删了数据就没了，所以把"变化的数据"挂载到宿主机目录，容器删了数据还在 |

一句话总结：**镜像 = 环境打包，容器 = 隔离运行，Compose = 多容器编排**。

---

## 二、我们项目的容器化架构

### 2.1 部署前 vs 部署后

```
┌──────────── 部署前（本地开发） ────────────┐
│                                           │
│  Windows 宿主机                            │
│  ├── MySQL 8.0 (D:\MySQL\... 直接装的)     │
│  ├── java -jar skill-gateway.jar :8080    │
│  ├── npm run dev (Vite) :5173             │
│  └── npm run dev (Admin) :5174            │
│                                           │
│  问题：                                    │
│  - 环境依赖散落各处，换个电脑要重装所有东西    │
│  - 配置不一致（Java版本/MySQL版本/Node版本） │
│  - 没有隔离，端口冲突、进程残留               │
└───────────────────────────────────────────┘

┌──────────── 部署后（Docker 容器化）─────────┐
│                                           │
│  Windows 宿主机 (只跑 Docker Desktop)       │
│                                           │
│  ┌── prometheus-net（容器内部网络）────┐     │
│  │                                    │     │
│  │  [prometheus-mysql]     :3306     │     │
│  │  [prometheus-backend]   :8080     │     │
│  │  [prometheus-frontend-user] :80   │     │
│  │  [prometheus-frontend-admin]:80   │     │
│  │                                    │     │
│  └────────────────────────────────────┘     │
│        ↑ 端口映射到宿主机                    │
│   :3307  :8080  :5173  :5174               │
│                                           │
│  好处：                                    │
│  - 一句 docker compose up 全部启动          │
│  - 内核级隔离，互不干扰                      │
│  - 环境统一，换电脑只需安装 Docker           │
└───────────────────────────────────────────┘
```

### 2.2 四个容器详解

```
docker-compose.yml
│
├── mysql             镜像: mysql:8.0 官方镜像
│   ├─ 环境变量注入:  MYSQL_ROOT_PASSWORD=root, MYSQL_DATABASE=prometheus_skill_bank
│   ├─ 端口映射:      3307(宿主机) → 3306(容器内)    ← 避让宿主机已有的 3306 MySQL
│   ├─ 数据卷:        mysql-data:/var/lib/mysql     ← 数据库文件持久化
│   ├─ 初始化:        init.sql 自动挂载到 /docker-entrypoint-initdb.d/
│   │                ← MySQL 官方镜像约定：该目录下的 .sql 会在首次启动时自动执行
│   └─ 健康检查:      mysqladmin ping，5 次重试，间隔 10s
│
├── backend           镜像: 我们自定义构建（Dockerfile）
│   ├─ 基础镜像:      eclipse-temurin:17-jre-alpine（只含 JRE，无 JDK，~80MB）
│   ├─ 构建产物:      COPY skill-gateway/target/skill-gateway-1.0.2.jar app.jar
│   ├─ JVM 配置:      -Xmx256m -Xms256m（堆内存固定 256MB）
│   ├─ 数据卷:        uploads:/app/uploads  ← 用户上传头像等文件持久化
│   ├─ 依赖等待:      等 MySQL healthy 后才启动
│   └─ 数据库连接:    用 docker-compose 服务名 "mysql" 作为主机名
│                    ← Docker 内部 DNS 自动解析为容器 IP
│
├── frontend-user     镜像: 我们自定义 → 多阶段构建（Dockerfile）
│   ├─ 阶段1: node:20-alpine 容器内 npm install + npm run build
│   ├─ 阶段2: nginx:alpine 容器内只放构建后的 dist/ 静态文件
│   │  ← 多阶段构建：构建工具不进入最终镜像，最终镜像 ~15MB
│   ├─ nginx.conf 中 /api → proxy_pass http://backend:8080
│   │  ← 不用 Vite 代理了，nginx 直接反代到 backend 容器
│   └─ 端口映射:      5173(宿主机) → 80(容器内 nginx)
│
└── frontend-admin    同上，端口 5174 → 80
```

### 2.3 关键设计决策

**为什么 MySQL 端口映射成 3307？**

宿主机已经装了 MySQL 占用 3306。容器内部 MySQL 也用 3306，映射到宿主机 3307 避免冲突。DBeaver/Navicat 连 `localhost:3307` 即可。

**为什么 nginx 反代而不用 Vite 代理？**

开发时 Vite 的 `vite.config.js` 里配了 `proxy: { '/api': 'http://localhost:8080' }`，这是 Vite dev server 做的。容器里没有 Vite dev server（只有 nginx），所以改用 nginx 的 `proxy_pass` 做同样的事。

**为什么 backend 连接 `mysql:3306` 而不是 `localhost:3306`？**

Docker Compose 会自动创建一个虚拟网络 `prometheus-net`，每个容器可以通过**服务名**互相访问。`mysql` 是 docker-compose.yml 里的服务名，Docker 内部 DNS 会自动解析为容器 IP。这是 Docker 最精妙的设计之一。

---

## 三、部署前后命令对比（直观感受）

### 3.1 启动命令

| 步骤 | 部署前（本地开发） | 部署后（Docker） |
|------|-------------------|-----------------|
| 1. 启动数据库 | `net start MySQL80`（Windows 服务） | 无需手动操作，compose 自动拉起 |
| 2. 初始化数据库 | `mysql -u root -proot < database/init.sql` | 无需手动操作，首次启动自动执行 |
| 3. 导入演示数据 | `mysql -u root -proot < database/seed_demo.sql` | ⚠️ 需手动：`docker exec -i prometheus-mysql mysql -uroot -proot prometheus_skill_bank < database/seed_demo.sql` |
| 4. 构建后端 | `cd skill-time-bank && mvn clean package -DskipTests -pl skill-gateway -am` | 同上（需先本地 mvn package 生成 JAR） |
| 5. 启动后端 | `java -jar skill-gateway/target/skill-gateway-1.0.2.jar` | `docker compose up -d backend` |
| 6. 启动用户端 | `cd skill-time-bank-web && npm install && npm run dev` | `docker compose up -d frontend-user` |
| 7. 启动管理后台 | `cd skill-admin-web && npm install && npm run dev` | `docker compose up -d frontend-admin` |

**Docker 一键全部启动：**

```bash
cd 项目代码

# 首次启动（会自动构建镜像，约 3-5 分钟）
docker compose up -d

# 之后再次启动（镜像已有，瞬间启动）
docker compose up -d
```

**对比感受：** 部署前要开 3 个终端窗口分别跑 JAR / Vite / Vite admin，部署后一句 `docker compose up -d` 全部搞定。环境依赖也不用装——新队友只需装 Docker Desktop，clone 代码后跑这一句就行。

### 3.2 停止命令

```bash
# 部署前：Ctrl+C 三个窗口，net stop MySQL80
# 部署后：
docker compose down           # 停止全部
```

### 3.3 查看状态

```bash
# 部署前：看控制台输出 + 浏览器访问检查
# 部署后：
docker compose ps                        # 所有容器状态
docker compose logs backend              # 看后端日志
docker compose logs -f backend           # 实时跟踪后端日志（类似 tail -f）
docker compose logs --tail=50 frontend-user
```

---

## 四、修改代码后的影响

这是最容易困惑的地方。**容器里的代码是"冻住"的，改源码不会自动生效。**

### 4.1 改后端代码后

```bash
# 必须三步：
1. mvn clean package -DskipTests -pl skill-gateway -am   # 重新打包 JAR
2. docker compose up -d --build backend                   # 重新构建镜像 + 重建容器
   # 或简写：
   docker compose up -d --build   # 重建所有有变化的服务
```

**原理：** `docker-compose.yml` 中 backend 用的是 `build`（不是 `image`），每次 rebuild 会重新执行 Dockerfile——重新 COPY 新的 JAR 包。不改代码只 `docker compose restart` 的话，容器里还是旧的 JAR。

### 4.2 改前端代码后

```bash
# 同样需要重建：
docker compose up -d --build frontend-user    # 或 frontend-admin
```

**原理：** 前端 Dockerfile 用了多阶段构建，`COPY . .` 和 `npm run build` 发生在 build 阶段。改代码后必须 rebuild，nginx 才会托管新的 dist/ 文件。

### 4.3 改数据库后

```bash
# 情况1：改了 init.sql（建表语句或初始数据）
docker compose down -v    # -v 删除数据卷，数据库会重置
docker compose up -d      # 首次启动重新执行 init.sql

# 情况2：只改了数据（INSERT/UPDATE），不需要重建
# 直接连容器内 MySQL 执行 SQL：
docker exec -i prometheus-mysql mysql -uroot -proot prometheus_skill_bank < some.sql
```

### 4.4 改 docker-compose.yml 后

```bash
docker compose up -d      # docker compose 会检测配置变化，自动重建需要重建的容器
```

### 4.5 啥情况下不需要重建？

| 场景 | 需要重建吗 | 说明 |
|------|:--:|------|
| 改 Java/Vue 源代码 | ✅ 需要 | rebuild 镜像 |
| 改 nginx.conf | ✅ 需要 | 前端镜像 rebuild |
| 改 docker-compose.yml | 自动检测 | compose 自己判断 |
| 改 application.yml（后端配置） | ✅ 需要 | 配置在 JAR 里打包了 |
| 改数据库数据 | ❌ 不需要 | 直接执行 SQL |
| 上传的头像文件 | ❌ 不需要 | 在数据卷里，独立于容器 |
| 改 CLADE.md 或文档 | ❌ 不需要 | 跟容器完全无关 |

### 4.6 快速开发迭代建议

```bash
# 如果频繁改代码，可以分模式：

# 后端开发模式：改后端代码后只重建后端
mvn clean package -DskipTests -pl skill-gateway -am && docker compose up -d --build backend

# 用户端开发模式：改前端后只重建用户端
docker compose up -d --build frontend-user
```

---

## 五、部署原理速查（各 Dockerfile 做了什么）

### backend Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine    # ① 基于轻量 JRE 镜像（只有运行环境，没有编译工具）
ENV TZ=Asia/Shanghai                  # ② 时区设定
WORKDIR /app                          # ③ 工作目录
COPY ... app.jar                      # ④ 把打好的 fat JAR 复制进镜像
EXPOSE 8080                           # ⑤ 声明端口（文档性质，实际映射在 compose 里配）
ENTRYPOINT ["java", "-Xmx256m", ...]  # ⑥ 容器启动时执行的命令 = java -jar app.jar
```

### frontend Dockerfile（多阶段构建）

```dockerfile
# ── 阶段1：构建阶段（这个阶段最终会被丢弃，不进入最终镜像）──
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./      # 先复制 package.json（利用 Docker 缓存层）
RUN npm install            # 安装依赖（仅依赖变了才会重跑这层）
COPY . .                   # 复制全部源码
RUN npm run build          # 构建，输出 dist/

# ── 阶段2：运行阶段（这才是最终镜像）──
FROM nginx:alpine                          # ① 基于极简 nginx 镜像
COPY --from=build /app/dist /usr/share/nginx/html  # ② 从阶段1把 dist/ 拷贝过来
COPY nginx.conf /etc/nginx/conf.d/default.conf     # ③ 覆盖 nginx 默认配置
CMD ["nginx", "-g", "daemon off;"]         # ④ 前台运行 nginx（后台运行容器会退出）
```

---

## 六、常见问题

**Q: 容器删了数据还在吗？**

A: 容器进程删了就没了（类似删虚拟机），但我们在 `docker-compose.yml` 里挂了两个数据卷：
- `mysql-data` — 数据库文件，`docker compose down -v` 才会删（-v = 同时删卷）
- `uploads` — 用户上传文件

普通 `docker compose down` 不删卷，数据安全。

**Q: 怎么进容器内部看看？**

```bash
docker exec -it prometheus-backend sh        # 进后端容器
docker exec -it prometheus-mysql mysql -uroot -proot  # 进 MySQL
docker exec -it prometheus-frontend-user sh  # 进前端 nginx
```

**Q: 容器内改的文件会持久化吗？**

A: 不会。容器文件系统是临时的，容器重建就丢失。只有挂载到数据卷的目录才持久化。

**Q: 如果我同时运行了本地 MySQL 和 Docker MySQL 会冲突吗？**

A: 不会。本地 MySQL 在 3306，Docker MySQL 映射在 3307。两个 MySQL 独立运行，彼此无感知。

---

## 七、常用命令速查表

```bash
docker compose up -d              # 启动所有服务（后台）
docker compose down               # 停止并删除所有容器（保留数据卷）
docker compose down -v            # 停止 + 删除容器 + 删除数据卷（⚠️ 数据库重置）
docker compose restart backend    # 重启单个服务（代码不变时用）
docker compose up -d --build      # 重建镜像并启动（代码变了用这个）
docker compose ps                 # 查看所有容器状态
docker compose logs -f backend    # 实时查看后端日志
docker compose logs --tail=100    # 查看最后 100 行日志
docker compose exec backend sh    # 进入后端容器内部
docker compose pull               # 拉取最新基础镜像（如 mysql:8.0）
```

---

## 八、总结

| 维度 | 部署前 | 部署后 |
|------|--------|--------|
| 启动命令 | 4 步，3 个终端 | 1 句 `docker compose up -d` |
| 环境依赖 | 装 JDK/Node/MySQL | 只装 Docker Desktop |
| 版本一致性 | 每人本地可能不同 | 完全一致（环境即代码） |
| 改代码生效 | 热更新/重启 | 需 rebuild 镜像 |
| 数据安全 | 删了就是删了 | 数据卷持久化，不随容器销毁 |
| 资源隔离 | 无 | 每个容器独立文件系统/网络/进程 |
| 迁移 | 重装环境 | copy 代码 + `docker compose up` |
