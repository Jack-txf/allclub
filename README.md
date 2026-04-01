# txf-allclub 俱乐部综合管理平台

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.4.2%2F3.0.2%2F3.5.11-green" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-8%2F17%2F21-blue" alt="Java">
  <img src="https://img.shields.io/badge/MySQL-8.0-orange" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-6.x-red" alt="Redis">
  <img src="https://img.shields.io/badge/Nacos-2.x-brightgreen" alt="Nacos">
  <img src="https://img.shields.io/badge/RocketMQ-5.x-yellow" alt="RocketMQ">
</p>

## 📖 项目介绍

`txf-allclub` 是一个综合型俱乐部管理平台，采用微服务架构设计，包含用户权限管理、社交分享、练习题库、即时通讯(IM)、RAG知识库等多个核心模块。项目基于Spring Boot生态构建，支持分布式部署和弹性扩展。

### 核心特性

- **模块化设计**：各业务模块独立开发、部署、扩展
- **多版本兼容**：支持Java 8/17/21，Spring Boot 2.x/3.x
- **微服务架构**：基于Nacos实现服务注册发现与配置中心
- **高性能通信**：WebSocket实时推送、RocketMQ异步消息、Dubbo RPC调用
- **云原生支持**：容器化部署友好，支持监控与链路追踪

---

## 🏗️ 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        接入层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   网关服务    │  │  WebSocket   │  │   微信回调    │       │
│  │  Gateway     │  │   Gateway    │  │    wx        │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      业务服务层                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │
│  │  auth    │ │  circle  │ │ practice │ │  subject │        │
│  │ 权限服务  │ │ 社交服务  │ │ 练习服务  │ │ 题库服务  │        │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │
│  │    im    │ │   rag    │ │ interview│ │   oss    │        │
│  │ 即时通讯  │ │ 知识库   │ │ 面试服务  │ │ 对象存储  │        │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       基础设施层                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │
│  │   MySQL  │ │  Redis   │ │  Nacos   │ │ RocketMQ │        │
│  │   8.x    │ │   6.x    │ │   2.x    │ │   5.x    │        │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                      │
│  │  Milvus  │ │ XXL-Job  │ │  Druid   │                      │
│  │ 向量库    │ │ 任务调度  │ │ 连接池   │                      │
│  └──────────┘ └──────────┘ └──────────┘                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 功能模块

### 1. 用户权限模块 (allclub-auth)

**端口**: 8810  
**Java版本**: 8  
**Spring Boot**: 2.4.2

- ✅ 用户注册、登录、登出
- ✅ JWT + Sa-Token 双令牌机制
- ✅ RBAC 角色权限管理
- ✅ 用户信息管理（头像、昵称、个人资料）
- ✅ 基于Redis的分布式会话
- ✅ 接口权限拦截控制

**模块结构**:
```
allclub-auth/
├── auth-api/              # API接口定义
├── auth-application/      # 应用层
│   ├── auth-app-controller/  # Web控制器
│   ├── auth-app-job/         # 定时任务
│   └── auth-app-mq/          # 消息监听
├── auth-domain/           # 领域层（实体、服务）
├── auth-infra/            # 基础设施层（Mapper）
├── auth-common/           # 公共工具
└── auth-starter/          # 启动模块
```

---

### 2. 社交分享模块 (allclub-circle)

**Java版本**: 8  
**Spring Boot**: 2.4.2

- ✅ 社交圈子创建与管理
- ✅ 动态发布（图文、视频）
- ✅ 评论、点赞、收藏功能
- ✅ 消息通知机制
- ✅ 敏感词过滤（DFA算法）
- ✅ WebSocket 实时消息推送

---

### 3. 练习题库模块 (allclub-practice)

**Java版本**: 8  
**Spring Boot**: 2.4.2

- ✅ 题目分类与标签管理
- ✅ 多种题型支持（单选、多选、判断、填空、简答）
- ✅ 练习记录与答题提交
- ✅ 智能组卷算法
- ✅ 排行榜与答题报告
- ✅ 错题本功能

---

### 4. 题目中心模块 (allclub-subject)

**Java版本**: 8  
**Spring Boot**: 2.4.2

- ✅ 题目CRUD管理
- ✅ 题目审核流程
- ✅ 标签体系管理
- ✅ 题目导出/导入（Excel）
- ✅ 题目统计分析

---

### 5. 即时通讯模块 (allclub-im)

**Java版本**: 17  
**Spring Boot**: 3.0.2  
**核心框架**: Netty + WebSocket + Dubbo

采用分布式IM架构设计：

```
┌──────────────────────────────────────────────────────────┐
│  客户端（Web/iOS/Android）                                │
└──────────────────────────────────────────────────────────┘
                            │
           ┌────────────────┼────────────────┐
           ▼                ▼                ▼
┌─────────────────┐ ┌───────────────┐ ┌─────────────────┐
│  tim-http-      │ │  tim-ws-      │ │  tim-auth/      │
│  gateway        │ │  gateway      │ │  auth-server    │
│  HTTP网关       │ │ WebSocket网关 │ │ 认证中心        │
└─────────────────┘ └───────────────┘ └─────────────────┘
           │                │                │
           └────────────────┼────────────────┘
                            ▼
┌──────────────────────────────────────────────────────────┐
│              tim-im-server/im-server                     │
│                   核心业务服务器                          │
│  - 单聊/群聊消息处理                                      │
│  - 消息持久化与漫游                                       │
│  - 在线状态管理                                           │
│  - 消息推送策略                                           │
└──────────────────────────────────────────────────────────┘
```

**功能特性**:
- ✅ 单聊与群聊消息
- ✅ 消息已读回执
- ✅ 消息撤回与删除
- ✅ 文件/图片传输
- ✅ 心跳机制与断线重连
- ✅ 消息持久化与历史消息拉取

---

### 6. RAG知识库模块 (all-rag) ⭐ 新增

**端口**: 8080  
**Java版本**: 21  
**Spring Boot**: 3.5.11

基于检索增强生成（RAG）的智能知识库系统：

```
文档上传 → 文档解析(Tika) → 文本分块 → 向量化 → Milvus存储
                                              ↓
用户提问 ← 答案生成(LLM) ← 上下文组装 ← 向量检索
```

**核心功能**:
- ✅ 多格式文档解析（PDF、Word、PPT、Excel、Markdown、TXT）
- ✅ 智能文档分块策略（固定大小、段落、递归、语义、滑动窗口）
- ✅ 向量数据库存储与检索（Milvus）
- ✅ 多模型支持（SiliconFlow API）
- ✅ 流式对话响应
- ✅ 文档元数据提取

**技术亮点**:
- Apache Tika 3.2.3 全格式文档解析
- Milvus 2.6.6 高性能向量检索
- 可配置的分块策略与参数
- 限流保护（Guava RateLimiter）
- Prometheus监控指标

---

### 7. 微信服务模块 (allclub-wx)

**Java版本**: 8  
**Spring Boot**: 2.4.2

- ✅ 微信消息回调处理
- ✅ 订阅号/服务号消息解析
- ✅ 模板消息推送
- ✅ 用户消息路由分发

---

### 8. 对象存储模块 (allclub-oss)

**Java版本**: 8  
**Spring Boot**: 2.4.2

- ✅ 文件上传/下载
- ✅ 图片压缩与缩略图生成
- ✅ 分片上传与大文件支持
- ✅ 存储策略（本地/阿里云OSS/MinIO可扩展）

---

### 9. 面试模块 (allclub-interview)

**Java版本**: 8  
**Spring Boot**: 2.4.2

- ✅ 面试题管理
- ✅ 模拟面试流程
- ✅ 面试评价与反馈
- ✅ 面试预约 scheduling

---

### 10. 任务调度中心 (xxl-job)

**Java版本**: 8  
**Spring Boot**: 2.4.2

- ✅ 分布式定时任务调度
- ✅ 任务管理与监控
- ✅ 执行日志与告警
- ✅ 多种路由策略（轮询、一致性哈希等）
- ✅ 失败重试与故障转移

---

### 11. 网关服务 (allclub-gateway)

**Java版本**: 8  
**Spring Boot**: 2.4.2  
**框架**: Spring Cloud Gateway

- ✅ 统一入口路由
- ✅ JWT鉴权与Sa-Token集成
- ✅ 限流与熔断
- ✅ 请求/响应日志
- ✅ 跨域处理

---

## 🛠️ 环境要求

### 基础环境

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 8/17/21 | 不同模块要求不同 |
| Maven | 3.6+ | 项目构建 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 缓存与会话 |
| Nacos | 2.0+ | 注册中心与配置中心 |

### 可选组件

| 组件 | 版本 | 说明 |
|------|------|------|
| RocketMQ | 5.0+ | 消息队列（IM模块必需） |
| Milvus | 2.3+ | 向量数据库（RAG模块必需） |
| XXL-Job | 2.4+ | 任务调度中心 |

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://gitee.com/quercus-sp204/txf-allclub.git
cd txf-allclub
```

### 2. 初始化数据库

```bash
# 创建数据库（根据各模块sql文件执行）
mysql -u root -p

CREATE DATABASE txf_club_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE tim_all CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE xxl_job CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 启动中间件

```bash
# 启动Nacos（配置与注册中心）
sh startup.sh -m standalone

# 启动Redis
redis-server

# 启动RocketMQ（IM模块需要）
sh mqnamesrv
sh mqbroker -n localhost:9876

# 启动Milvus（RAG模块需要）
docker run -d --name milvus -p 19530:19530 milvusdb/milvus:latest
```

### 4. 编译项目

```bash
# 编译认证模块
cd allclub-auth
mvn clean install -DskipTests

# 编译RAG模块
cd ../all-rag
mvn clean install -DskipTests

# 编译IM模块
cd ../allclub-im
mvn clean install -DskipTests
```

### 5. 启动服务

**建议启动顺序**：

```bash
# 1. 启动网关（统一入口）
cd allclub-gateway
mvn spring-boot:run

# 2. 启动基础服务（Auth）
cd allclub-auth/auth-starter
mvn spring-boot:run

# 3. 启动业务服务
cd allclub-circle/circle-server
mvn spring-boot:run

cd allclub-practice/practice-server
mvn spring-boot:run

cd allclub-subject/subject-starter
mvn spring-boot:run

# 4. 启动IM服务（按顺序）
cd allclub-im/tim-auth/auth-server
mvn spring-boot:run

cd allclub-im/tim-im-server/im-server
mvn spring-boot:run

cd allclub-im/tim-ws-gateway
mvn spring-boot:run

cd allclub-im/tim-http-gateway
mvn spring-boot:run

# 5. 启动RAG服务
cd all-rag
mvn spring-boot:run

# 6. 启动其他服务
cd allclub-wx
mvn spring-boot:run

cd xxl-job/xxl-job-admin
mvn spring-boot:run
```

---

## ⚙️ 配置说明

### 数据库配置 (application.yml)

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/txf_club_db?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
    type: com.alibaba.druid.pool.DruidDataSource
```

### Redis配置

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 123456
    database: 4
```

### Nacos配置 (bootstrap.yml)

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
        file-extension: yaml
      discovery:
        server-addr: 127.0.0.1:8848
```

### RAG模块配置 (application.yml)

```yaml
rag:
  datasource:
    parser:
      parse-timeout-seconds: 60
      max-file-size-bytes: 209715200
  vector:
    type: milvus
    milvus:
      uri: http://127.0.0.1:19530
      collection:
        dimension: 2560
        index-type: HNSW
        metric-type: COSINE
```

---

## 📁 项目结构

```
txf-allclub/
├── allclub-auth/              # 用户权限模块
│   ├── auth-api/
│   ├── auth-application/
│   ├── auth-common/
│   ├── auth-domain/
│   ├── auth-infra/
│   └── auth-starter/
├── allclub-circle/            # 社交分享模块
├── allclub-gateway/           # 网关服务
├── allclub-im/                # 即时通讯模块
│   ├── tim-auth/
│   ├── tim-http-gateway/
│   ├── tim-im-server/
│   └── tim-ws-gateway/
├── allclub-interview/         # 面试模块
├── allclub-oss/               # 对象存储模块
├── allclub-practice/          # 练习题库模块
├── allclub-subject/           # 题目中心模块
│   ├── subject-api/
│   ├── subject-application/
│   ├── subject-common/
│   ├── subject-domain/
│   ├── subject-infra/
│   └── subject-starter/
├── allclub-wx/                # 微信服务模块
├── all-rag/                   # RAG知识库模块 ⭐
│   ├── src/main/java/com/feng/rag/
│   │   ├── chunk/            # 文档分块
│   │   ├── controller/       # API控制器
│   │   ├── datasource/       # 文档解析
│   │   ├── model/            # AI模型
│   │   ├── retrieval/        # 检索逻辑
│   │   └── vector/           # 向量数据库
│   └── src/main/resources/application.yml
├── xxl-job/                   # 任务调度
│   ├── xxl-job-admin/        # 调度中心
│   └── xxl-job-core/         # 客户端核心
└── aasync-project/            # 异步工具项目
```

---

## 🔧 开发指南

### 模块依赖关系

```
auth-starter
├── auth-app-controller (依赖: auth-domain, auth-infra, auth-api)
├── auth-app-job (依赖: auth-domain, auth-infra)
├── auth-app-mq (依赖: auth-domain, auth-infra)
├── auth-domain (依赖: auth-api, auth-common)
├── auth-infra (依赖: auth-domain)
└── auth-common
```

### 代码规范

- **包名**: `com.feng.{module}.{layer}`
- **实体类**: 放在 `domain/entity`，使用MyBatis Plus注解
- **Mapper**: 放在 `infra/mapper`，继承 `BaseMapper`
- **Service**: 放在 `domain/service`，接口+实现分离
- **Controller**: 放在 `application/controller`，RESTful API设计

### 新增模块步骤

1. 在根目录创建新模块文件夹
2. 编写 `pom.xml`，指定父模块或独立版本
3. 参考现有模块结构创建 `application`, `domain`, `infra` 层
4. 创建启动类和 `application.yml`
5. 在Nacos中添加对应配置文件

---

## 🔍 监控与运维

### Actuator端点

RAG模块已集成Spring Boot Actuator：

```
GET /actuator/health      # 健康检查
GET /actuator/metrics     # 指标数据
GET /actuator/prometheus  # Prometheus格式指标
```

### 日志配置

各模块日志配置文件：`src/main/resources/log4j2-spring.xml`

日志级别建议：
- 开发环境：DEBUG
- 测试环境：INFO
- 生产环境：WARN

---

## 📚 技术栈详解

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.4.2/3.0.2/3.5.11 | 基础框架 |
| Spring Cloud Gateway | 2020.0.6/2022.0.5 | API网关 |
| Spring Cloud Alibaba | 2021.1/2022.0.0.0 | 微服务组件 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| Redis | 6.x | 缓存/会话 |
| Druid | 1.2.20 | 连接池 |
| Sa-Token | 1.37.0 | 权限认证 |
| Apache Dubbo | 3.3.0 | RPC框架 |
| RocketMQ | 5.x | 消息队列 |
| XXL-Job | 2.4.0 | 任务调度 |
| Apache Tika | 3.2.3 | 文档解析 |
| Milvus SDK | 2.6.6 | 向量数据库 |
| Netty | 4.1.x | 网络通信 |
| Lombok | 1.18.x | 代码简化 |

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📄 开源协议

本项目基于 [Apache-2.0](LICENSE) 协议开源。

---

## 👤 作者信息

- **作者**: txf / Williams_Tian
- **邮箱**: [your-email@example.com]
- **Gitee**: https://gitee.com/quercus-sp204

---

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [Sa-Token](https://sa-token.cc/)
- [XXL-Job](https://www.xuxueli.com/xxl-job/)
- [Milvus](https://milvus.io/)
- [Apache Tika](https://tika.apache.org/)

---

<p align="center">
  <b>Star ⭐ 本项目 if it helps you!</b>
</p>
