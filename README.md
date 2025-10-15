

# txf-allclub

## 介绍

txf-allclub 是一个综合型俱乐部管理平台，包含用户权限管理、社交分享、练习题库、微信服务等多个模块。该项目基于Spring Boot、MyBatis Plus、Redis、Feign等主流Java技术栈构建，</br>
支持微服务架构与分布式部署。

### 主要功能模块

1. **用户权限模块 (allclub-auth)**  
   - 用户注册、登录、信息管理
   - 角色管理、权限分配
   - 使用Redis进行用户状态与权限缓存
   - 支持拦截器控制接口权限访问

2. **社交分享模块 (allclub-circle)**  
   - 社交圈子创建、管理
   - 发布、评论、消息通知
   - 敏感词过滤机制
   - WebSocket 实时通信支持

3. **练习题库模块 (allclub-practice)**  
   - 题目分类、标签管理
   - 练习记录、答题提交
   - 支持多种题型（单选、多选、判断等）
   - 排行榜与答题报告功能

4. **微信服务 (allclub-wx)**  
   - 微信消息回调处理
   - 支持订阅与文本消息处理
   - 用户消息解析与路由

5. **作业调度 (xxl-job)**  
   - 支持定时任务调度
   - 提供作业管理、日志、失败监控等
   - 支持多种调度策略（轮询、一致性哈希等）

## 技术架构

- **Spring Boot** 作为基础框架
- **MyBatis Plus** 数据库操作层
- **Redis** 用户信息、权限、敏感词等缓存
- **Feign** 模块间通信
- **WebSocket** 实时消息推送
- **RocketMQ** 异步消息队列
- **XXL-JOB** 定时任务调度平台

## 安装与部署

1. 克隆项目：
   ```bash
   git clone https://gitee.com/quercus-sp204/txf-allclub.git
   ```

2. 构建Maven项目：
   ```bash
   cd txf-allclub
   mvn clean install
   ```

3. 配置数据库、Redis、MQ等服务（查看 `application.yml` 或 `bootstrap.yml`）

4. 启动各个模块：
   ```bash
   # 启动认证服务
   cd allclub-auth/auth-starter
   mvn spring-boot:run

   # 启动社交模块
   cd allclub-circle/circle-server
   mvn spring-boot:run

   # 启动微信服务
   cd allclub-wx
   mvn spring-boot:run

   # 启动定时任务调度
   cd xxl-job/xxl-job-admin
   mvn spring-boot:run
   ```
   
## IM更新
**需要的中间件**：Redis、Nacos、MySQL8.x、消息队列MQ【RocketMQ】
### 软件架构
![](.\zimages\架构1.png)

上图就是该项目的大致架构说明。为什么要这么设计呢？下图给出原因:

![](.\zimages\原因1.png)

