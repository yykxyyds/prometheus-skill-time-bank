# Prometheus — 技能时间银行

以"时间币"为核心的技能互助平台：用户通过提供技能赚取时间币，再用时间币消费他人技能。

## 技术栈

| 层   | 技术 |
|------|------|
| 后端 | Spring Boot 3.2 + MyBatis-Plus 3.5.5 + Java 17 |
| 数据库 | MySQL 8.0，15张表，库名 prometheus_skill_bank |
| 前端 | Vue 3 + Vite 8 + Element Plus + Pinia + Axios |
| 认证 | JWT (jjwt 0.12.3) + @RequireAuth 自定义注解 |
| API文档 | SpringDoc OpenAPI 2.3 |
| 部署 | Docker Compose (4容器: MySQL + Backend + 用户端 + 管理后台) |
| 构建 | Maven 多模块聚合工程（7子模块） |

## 项目结构

```
code/
├── database/init.sql              # 15张表 DDL + 初始数据
├── skill-time-bank/               # 后端 Spring Boot 多模块工程
│   ├── skill-common/              # 统一Result/JWT/异常/BaseEntity
│   ├── skill-user-service/        # 用户注册登录/关注/个人信息
│   ├── skill-skill-service/       # 技能广场/分类搜索/需求悬赏
│   ├── skill-order-service/       # 订单状态机/聊天/私信
│   ├── skill-wallet-service/      # 钱包/时间流水/评价/申诉/公告
│   ├── skill-admin-service/       # 管理员后台
│   └── skill-gateway/             # 统一启动入口
├── skill-time-bank-web/           # 用户端 Vue 3 前端
└── skill-admin-web/               # 管理后台 Vue 3 前端
```

## 课程加分项

| 加分项 | 状态 | 说明 |
|--------|:--:|------|
| 安全认证 JWT | ✅ | @RequireAuth 注解 + 全局拦截器 |
| 设计模式 (3种) | ✅ | 状态模式(订单) + 拦截器模式(JWT) + 模板方法(BaseEntity)，已写入详细设计文档第6章 |
| Swagger 接口文档 | ✅ | SpringDoc OpenAPI 2.3 |
| 单元测试 (59个) | ✅ | OrderServiceImpl(30) + UserServiceImpl(29)，JUnit 5 + Mockito |
| Docker 部署 | ✅ | Docker Compose 4容器编排 |

## 快速开始

```bash
# 1. 启动 MySQL（端口 3306）
# 2. 建库 + 导数据
mysql -u root -proot < database/init.sql
mysql -u root -proot prometheus_skill_bank < database/seed_demo.sql

# 3. 启动后端（端口 8080）
cd skill-time-bank
mvn clean package -DskipTests -pl skill-gateway -am
java -jar skill-gateway/target/skill-gateway-1.0.2.jar

# 4. 启动用户前端（端口 5173）
cd ../skill-time-bank-web
npm install && npm run dev

# 5. 启动管理后台（端口 5174）
cd ../skill-admin-web
npm install && npm run dev

# 6. Docker 一键部署
cd .. && docker compose up -d
```

## 运行测试

```bash
cd skill-time-bank
# 需要 JDK 17
export JAVA_HOME=/path/to/jdk-17
mvn test -pl skill-user-service,skill-order-service -am
```

## 团队

- 第10组，指导教师：刘德才
- 海南大学 2026年春季学期 综合实训课程设计
