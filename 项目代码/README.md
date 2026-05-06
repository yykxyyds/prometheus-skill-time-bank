# Prometheus——技能时间银行

> 海南大学课程设计 · 第10组 · 指导教师：刘德才

一个以"时间币"为核心的技能互助平台。用户通过提供技能赚取时间币，再用时间币消费他人技能。拒绝金钱交易，回归价值交换。

## 技术栈

| 层   | 技术                                               |
| --- | ------------------------------------------------ |
| 后端  | Spring Boot 3.2 + MyBatis-Plus 3.5.5 + Maven 多模块 |
| 数据库 | MySQL 8.0                                        |
| 前端  | Vue 3 + Vite + Element Plus + Pinia + Axios      |
| 认证  | JWT (jjwt 0.12.3)                                |
| 工具  | Hutool 5.8、Lombok、SpringDoc OpenAPI 2.3          |

## 拉取项目

```bash
cd "专门放项目代码的PATH"
git clone https://github.com/yykxyyds/prometheus-skill-time-bank.git
cd prometheus-skill-time-bank/项目代码
# 拉取后可以直接打开claude code让它帮你把代码运行起来，就不需要看下面的东西了【下面都是手动操作方法】
```

## 环境要求

- **JDK 17** 或以上
- **Maven 3.8** 或以上
- **Node.js 18** 或以上
- **MySQL 8.0** 或以上（已有建表脚本）

## 数据库初始化

1. 启动 MySQL 服务
2. 执行建表脚本：

```bash
mysql -u root -p < database/init.sql
```

> 脚本会创建 `prometheus_skill_bank` 数据库、14 张表，并插入初始数据（管理员 + 测试用户 + 8 个技能分类）。

## 后端启动

```bash
# 1. 进入后端项目
cd skill-time-bank

# 2. 编译
mvn clean compile

# 3. 打包（生成 fat JAR）
mvn clean package -DskipTests -pl skill-gateway -am

# 4. 启动（端口 8080）
java -jar skill-gateway/target/skill-gateway-1.0.0.jar
```

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

## 默认账户

| 角色   | 用户名        | 密码         |
| ---- | ---------- | ---------- |
| 管理员  | `admin`    | `admin123` |
| 测试用户 | `testuser` | `123456`   |
| 测试用户 | `test99`   | `123456`   |
| 测试用户 | `yykxyyds` | `123456`   |

## 项目结构

```
项目代码/
├── database/
│   └── init.sql                 # 14张表 DDL + 初始数据
├── skill-time-bank/             # 后端 Maven 多模块工程
│   ├── skill-common/            #   公共模块：Result、异常、JWT、BaseEntity
│   ├── skill-user-service/      #   用户模块：注册/登录/个人信息
│   ├── skill-skill-service/     #   技能模块：技能广场/分类/悬赏
│   ├── skill-order-service/     #   订单模块：订单状态机/聊天
│   ├── skill-wallet-service/    #   钱包模块：余额/评价/申诉/公告
│   └── skill-gateway/           #   聚合启动模块（统一入口，端口8080）
├── skill-time-bank-web/         # 前端 Vue 3 工程
│   └── src/
│       ├── api/                 #   Axios 封装 + 接口调用
│       ├── stores/              #   Pinia 状态管理
│       ├── router/              #   路由 + 守卫
│       └── views/               #   页面组件
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

## 团队

| 角色 | 姓名 |
| -- | -- |
| 组长 | —  |
| 组员 | —  |
| 组员 | —  |
| 组员 | —  |

> 请各成员在此填入姓名

