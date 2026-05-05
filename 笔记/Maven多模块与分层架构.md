# Maven 多模块（聚合工程）与分层架构

> 课程设计硬性要求：必须使用 Maven 聚合工程（多模块 Spring Boot 项目），不得使用单模块项目。

---

## 一、为什么需要多模块？

**单模块问题**：一个项目所有代码堆在一起，controller/service/mapper 全在一个目录下，随着功能增多：
- 代码耦合严重，互相牵扯
- 团队成员并行开发容易冲突
- 构建时间长（改一行也要全量编译）
- 难以复用公共代码

**多模块优势**：按功能/层级拆分成独立子模块，各管各的，修改一个不影响其他。

---

## 二、Maven 聚合工程结构

### 典型结构

```
skill-time-bank/                  # 父项目目录
├── pom.xml                       # 父 POM（聚合 + 继承）
│
├── skill-common/                 # 子模块：公共模块
│   └── pom.xml
│
├── skill-user-service/           # 子模块：用户服务
│   └── pom.xml
│
├── skill-skill-service/          # 子模块：技能货架服务
│   └── pom.xml
│
├── skill-order-service/          # 子模块：订单交易服务
│   └── pom.xml
│
├── skill-wallet-service/         # 子模块：时间银行服务
│   └── pom.xml
│
└── skill-gateway/                # 子模块：网关
    └── pom.xml
```

### 父 POM 写法

父项目 `pom.xml` 同时扮演两个角色：

**① 作为聚合工程**：用 `<modules>` 声明有哪些子模块，一条 `mvn install` 构建所有模块。

```xml
<groupId>com.prometheus</groupId>
<artifactId>skill-time-bank</artifactId>
<version>1.0.0</version>
<packaging>pom</packaging>   <!-- 父项目 packaging 必须为 pom -->

<modules>
    <module>skill-common</module>
    <module>skill-user-service</module>
    <module>skill-skill-service</module>
    <module>skill-order-service</module>
    <module>skill-wallet-service</module>
    <module>skill-gateway</module>
</modules>
```

**② 作为父项目**：用 `<dependencyManagement>` 统一管理依赖版本，子模块继承后不用写版本号。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>3.2.0</version>
        </dependency>
        <!-- 所有依赖版本号只在这里定义一次 -->
    </dependencies>
</dependencyManagement>
```

### 子模块 POM 写法

```xml
<parent>
    <groupId>com.prometheus</groupId>
    <artifactId>skill-time-bank</artifactId>
    <version>1.0.0</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<artifactId>skill-user-service</artifactId>

<dependencies>
    <!-- 子模块依赖 parent，但不需要写版本号 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- 依赖其他子模块 -->
    <dependency>
        <groupId>com.prometheus</groupId>
        <artifactId>skill-common</artifactId>
    </dependency>
</dependencies>
```

---

## 三、父子项目 vs 聚合项目

| 方式 | 核心机制 | 作用 |
|------|---------|------|
| **父子项目** | `<parent>` + `<dependencyManagement>` | **继承**：子模块继承父 POM 的统一版本管理，不用重复写版本号 |
| **聚合项目** | `<modules>` | **一次性构建**：父项目知道有哪些子模块，一条命令构建全部 |

> **实际开发中两者结合使用**：同一个父 POM 既声明 `<modules>`（聚合），又声明 `<dependencyManagement>`（继承）。

**类比理解**：
- **继承（父子）** = 学校统一校服标准（颜色/款式），各班按这个标准自己买，不用每个班各自定标准
- **聚合（modules）** = 校长点名，知道全校有几个班，一喊"集合"所有班都到

---

## 四、分层架构设计

### 层次划分

```
┌─────────────────────────────────────────┐
│         controller 层（表现层）           │  ← 接收请求、返回响应
│   UserController  SkillController       │
├─────────────────────────────────────────┤
│         service 层（业务逻辑层）           │  ← 核心业务逻辑处理
│   UserService  SkillService  OrderService│
├─────────────────────────────────────────┤
│         mapper 层（数据访问层）            │  ← 数据库 CRUD 操作
│   UserMapper  SkillMapper               │
├─────────────────────────────────────────┤
│         common（公共模块）                │  ← 工具类、统一响应、异常
│   Result  GlobalException  JwtUtil      │
└─────────────────────────────────────────┘
```

### 分层 vs 模块的映射关系

**两种组织方式**（二选一或混合）：

**方式 A：按层分包（适合小项目）**

```
skill-user-service/
├── controller/
│   └── UserController.java
├── service/
│   └── UserServiceImpl.java
└── mapper/
    └── UserMapper.java
```

**方式 B：按功能垂直拆分（推荐，本课程设计适用）**

```
skill-user-service/         ← 一个子模块就是一个"用户"功能域
├── controller/
├── service/
└── mapper/

skill-order-service/        ← 另一个子模块，独立部署/独立开发
├── controller/
├── service/
└── mapper/
```

> 方式 B 下，**每个子模块内部仍然是分层**（controller → service → mapper），但模块之间通过 API 调用通信（Feign/RestTemplate）。

---

## 五、关键规范

1. **父 POM 的 `<packaging>` 必须为 `pom`**，否则 Maven 无法识别父项目
2. **子模块通过 `<parent>` 指向父 POM**，继承版本管理
3. **公共依赖放在父 POM 的 `<dependencyManagement>`** 中，由 `<dependencies>` 引入的依赖所有子模块都会继承（慎用）
4. **各子模块的 `<artifactId>` 建议使用统一前缀**（如 `skill-`），方便识别
5. **子模块之间可以互相依赖**（如 order-service 依赖 common 和 user-service），但避免循环依赖

---

## 六、常见问题

**Q: 父项目的 `<dependencies>` 和 `<dependencyManagement>` 有什么区别？**

- `<dependencyManagement>`：**只声明版本号，不实际引入**。子模块需要某个依赖时，在自己的 pom 中声明（不用写版本），继承父项目的版本。
- `<dependencies>`：**子模块会自动继承**所有在父项目这里声明的依赖。一般只在父项目放所有子模块都必需的依赖，其余放 `<dependencyManagement>`。

**Q: 多模块项目怎么启动？**

方式一：在父项目目录执行 `mvn spring-boot:run -pl skill-user-service`（只启动 user-service 模块）
方式二：在子模块目录执行 `mvn spring-boot:run`
方式三：在 IDE 中直接运行子模块的 Spring Boot 主类（推荐开发时使用）

**Q: 子模块之间怎么调用？**

- 同模块内：直接 `@Autowired` 调用 service
- 跨模块调用子模块的接口：引入对应子模块的依赖，调它的 service 接口
- 微服务架构下（后续可选）：通过 Feign 进行远程调用
