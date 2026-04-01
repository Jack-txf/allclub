# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`txf-allclub` is a multi-module Java Spring Boot club management platform with microservices architecture. It contains user authentication, social circle, practice questions, IM chat, and RAG (Retrieval-Augmented Generation) modules.

## Build Commands

This is a Maven-based multi-module project. There is no root aggregator POM; each module is built independently.

```bash
# Build a specific module
cd <module-name> && mvn clean install

# Build examples
cd allclub-auth && mvn clean install
cd all-rag && mvn clean install

# Run a specific Spring Boot application
cd allclub-auth/auth-starter && mvn spring-boot:run
cd all-rag && mvn spring-boot:run
```

## Module Structure & Entry Points

Each module has its own main application class and port:

| Module | Entry Class | Port | Java Version |
|--------|-------------|------|--------------|
| allclub-auth | `com.feng.auth.AuthApplication` | 8810 | 8 |
| allclub-circle | `com.feng.circle.server.CircleApplication` | - | 8 |
| allclub-gateway | `com.feng.gateway.GateWayApplication` | - | 8 |
| allclub-im/tim-http-gateway | `com.feng.tim.httpgateway.HttpGateWayApplication` | - | 17 |
| allclub-im/tim-auth/auth-server | `com.feng.tim.auth.AuthApplication` | - | 17 |
| allclub-im/tim-im-server/im-server | `com.feng.tim.im.IMApplication` | - | 17 |
| allclub-practice | `com.feng.practice.server.PracticeApplication` | - | 8 |
| allclub-subject | `com.feng.subject.starter.SubjectApplication` | - | 8 |
| allclub-wx | `com.feng.wx.WxApplication` | - | 8 |
| all-rag | `com.feng.rag.RagApplication` | 8080 | 21 |
| xxl-job/xxl-job-admin | `com.xxl.job.admin.XxlJobAdminApplication` | - | 8 |

## Technology Versions by Module

**Legacy Modules (auth, circle, gateway, practice, subject, wx, xxl-job):**
- Java 8
- Spring Boot 2.4.2
- Spring Cloud Alibaba 2021.1
- MyBatis Plus
- Redis
- Nacos (config & discovery)

**IM Module (allclub-im):**
- Java 17
- Spring Boot 3.0.2
- Spring Cloud 2022.0.5
- Spring Cloud Alibaba 2022.0.0.0-RC2
- Apache Dubbo 3.3.0
- MyBatis Plus 3.5.5

**RAG Module (all-rag):**
- Java 21
- Spring Boot 3.5.11
- Apache Tika 3.2.3 (document parsing)
- Milvus SDK 2.6.6 (vector database)
- OkHttp 5.3.2

## Required Infrastructure

- **MySQL 8.x**: Database for persistent storage
- **Redis**: Caching and session storage
- **Nacos**: Service discovery and configuration center (port 8848)
- **RocketMQ**: Message queue (for IM module)
- **Milvus**: Vector database (for RAG module, port 19530)

## Architecture Patterns

### DDD-Style Layered Architecture (auth, subject modules)
```
auth-starter/          # Application entry point, config
├── auth-application/  # Application layer
│   ├── auth-app-controller/  # REST controllers
│   ├── auth-app-job/         # Scheduled jobs
│   └── auth-app-mq/          # Message queue handlers
├── auth-domain/       # Domain layer (entities, services, constants)
├── auth-infra/        # Infrastructure layer (mappers, data access)
├── auth-api/          # API definitions (DTOs, interfaces)
└── auth-common/       # Common utilities
```

### IM Module Architecture
```
allclub-im/
├── tim-http-gateway/  # HTTP gateway for IM
├── tim-auth/          # Authentication server (Dubbo services)
├── tim-ws-gateway/    # WebSocket gateway
└── tim-im-server/     # Core IM server
    ├── im-server-api/ # API definitions
    └── im-server/     # Implementation
```

### RAG Module Architecture
```
all-rag/
├── chunk/             # Document chunking strategies
├── controller/        # REST APIs
├── datasource/        # Document parsing (Tika)
├── model/             # AI model clients
├── retrieval/         # Retrieval logic
└── vector/            # Vector database (Milvus) operations
```

## Key Configuration Files

- `application.yml`: Main configuration with DB, Redis, server settings
- `bootstrap.yml`: Nacos config center connection settings
- `application.yml` (RAG): Contains `rag.*` properties for chunking, vector DB, and AI model configuration

## Common Development Notes

- **MyBatis Plus**: Uses `@MapperScan("com.feng.**.mapper")` pattern
- **Nacos**: Requires bootstrap.yml for config import (Spring Cloud 2020+ pattern)
- **Sa-Token**: Used for authentication in gateway module with token prefix `txf`
- **Dubbo**: Used in IM module for RPC between services

## Testing

Tests use standard JUnit 5 with Spring Boot Test:

```bash
cd <module-dir> && mvn test
```

Tests are primarily in the `xxl-job` module; other modules have minimal test coverage.
