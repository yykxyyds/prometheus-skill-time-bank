# Claude Code 多 Agent 功能与使用指南

> 记录时间：2026-05-05

---

## 什么是 Agent

Agent 是在当前对话里启动的**子会话**，有独立的上下文，不会互相干扰。可以理解为同时开多个"分身"各自干活。

## 基础参数

| 参数 | 作用 |
|------|------|
| `subagent_type` | `general-purpose`（默认，可读写）或 `Explore`（只读搜索） |
| `prompt` | 告诉 Agent 做什么，要写清楚 |
| `description` | 3-5 字简述 |
| `run_in_background` | `true` = 后台跑，`false` = 等它跑完才继续 |
| `isolation: "worktree"` | 在隔离的 git worktree 中运行 |

## 两种用法

### 串行派活，等结果

```
Agent A: "帮我建好父 POM 和模块结构"
→ 收到 A 的返回结果
→ 基于 A 的结果，再派 Agent B
```

### 并行派活，最后汇总（高效的关键）

```
Agent A: "实现用户模块"    ← 后台
Agent B: "实现技能模块"    ← 后台
Agent C: "实现订单模块"    ← 后台

→ 三个同时跑，我在前台干别的
→ 全部完成后统一检查和联调
```

## 适合用 Agent 的场景

| 场景 | 用 Agent？ |
|------|-----------|
| "查文件在哪" | ❌ 直接 Glob/Grep |
| "读文件内容" | ❌ 直接 Read |
| "实现整个用户模块" | ✅ Agent |
| "搜索代码中所有 Controller" | ❌ 直接 Grep |
| "分析包结构写报告" | ✅ Agent（Explore 类型） |

## 对 Prometheus 项目的打法

### 阶段 1：搭基础（可并行）

```
我手动: 定数据库表结构 + 接口约定

→ 并发派（后台）：
  Agent 1: 建表 SQL + 初始化数据
  Agent 2: 搭建 Maven 多模块骨架
  Agent 3: 写 README + 自动化脚本
```

### 阶段 2：实现功能模块（可并行）

```
→ 并发派（后台）：
  Agent 1: 用户模块（注册/登录/Security）
  Agent 2: 技能模块（发布/技能广场/悬赏）
  Agent 3: 订单模块（下单/交易/时间币）
  Agent 4: 评价模块（双盲评价/雷达图）
```

### 注意事项
1. Agent 看不到当前对话的历史，prompt 要自包含
2. Agent 直接写文件到磁盘，不返回代码到对话
3. 后台 Agent 完成时会收到通知，不用轮询
4. 多模块项目不建议用 `isolation: "worktree"`，模块间有依赖
