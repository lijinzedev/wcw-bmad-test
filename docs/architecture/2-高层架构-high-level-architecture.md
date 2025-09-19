# 2. 高层架构 (High Level Architecture)

## 2.1 技术摘要

本系统将采用**分层的单体应用架构**，部署于企业**私有数据中心**。前端（PC Web和移动App）通过 **Nginx** 作为反向代理和API网关，与后端Java应用进行通信。后端应用内部按业务领域进行模块化分层（如表现层、业务逻辑层、数据访问层），以保证代码的清晰度和可维护性。整体架构旨在满足PRD中对四级数据贯通和私有化部署的要求。

## 2.2 平台与基础设施选择

- **平台:** **企业私有数据中心 / 本地服务器 (Private Data Center / On-Premise Servers)**
- **理由:** 满足企业数据安全和自主可控的要求，不依赖公有云服务。
- **关键组件:**
  - **计算:** 物理服务器或虚拟化平台 (如 VMware vSphere)
  - **数据库:** 自建 PostgreSQL 数据库集群
  - **存储:** 自建 S3 兼容对象存储 (如 MinIO)
  - **网络:** 企业内部网络，Nginx 作为核心网关

## 2.3 代码仓库结构

- **结构:** **Monorepo (单体仓库)**
- **理由:** 便于管理前端、后端和移动端之间的共享代码，并简化统一的CI/CD流程。
- **管理工具:** Nx。

## 2.4 高层架构图

```
graph TD
    subgraph 用户端
        U1(一线员工 - 移动App)
        U2(各级管理员 - PC Web)
        U3(政府监管员 - PC Web)
    end

    subgraph 企业数据中心
        NGINX(Nginx - 反向代理/API网关)

        subgraph 应用服务器 (Docker Compose)
            App_BE(后端单体应用 - Java Spring Boot)
            App_FE(前端应用 - Vue.js)
        end

        subgraph 数据存储
            DB_PG(PostgreSQL 数据库)
            S3(对象存储 - MinIO)
        end

        subgraph 外部系统
            Ext1(安全监控系统)
            Ext2(短信/App推送网关)
        end

        U1 & U2 & U3 --> NGINX;
        NGINX --> App_FE;
        NGINX --> App_BE;
        App_BE --> DB_PG;
        App_BE --> S3;
        App_BE --> Ext1;
        App_BE --> Ext2;
    end
```

## 2.5 架构与设计模式

- **分层单体架构 (Layered Monolithic):** 应用被构建为一个单一的、统一的单元，内部通过逻辑分层（表示层、业务层、持久层、数据库层）来组织代码，实现关注点分离。
- **反向代理/API网关 (Reverse Proxy/API Gateway):** 使用Nginx作为所有客户端请求的单一入口，处理SSL卸载、负载均衡、路由和静态资源服务。
- **仓库模式 (Repository Pattern):** 在服务内部，数据访问逻辑将通过仓库层进行抽象，使业务逻辑与数据存储实现分离。

## 2.6 安全架构 (4A Architecture)

为了满足企业级安全需求，系统将基于4A架构理念进行设计：

- **认证 (Authentication):** 确认用户身份。我们将使用后端框架内置的安全模块（如Spring Security）实现用户登录和身份验证。
- **授权 (Authorization):** 管理用户权限。我们将基于PRD中定义的四级角色模型，实现精细化的功能和数据访问控制。
- **审计 (Auditing):** 记录用户行为。所有关键操作都将被详细记录到日志中，以便进行安全审计和事后追溯。
- **授权管理 (Administration):** 提供后台管理功能，允许系统管理员对用户、角色和权限进行配置。

## 2.7 集成策略与运行机制

- **监控系统轮询 (int-monitoring):**
  - 方式：基于 Spring Scheduling 的固定延迟轮询（`monitoring.poll-delay-ms`），初次运行按 `monitoring.lookback-minutes` 回溯窗口拉取外部 `/api/v1/alerts`。
  - 去重：若外部事件携带 `externalId`，在本地预警表中查重避免重复入库。
  - 阈值匹配：按 `metricCode` + `locationPattern`（包含匹配）应用启用中的阈值规则，比较符与数值命中则生成 `MonitoringAlert`。
  - 观测性：每次轮询写入 `MonitoringPollRun`，包含状态与摘要，便于审计与排障；网络/解析异常具备有限重试与告警日志。

- **通知联动 (int-notification):**
  - 触发：当生成 `MonitoringAlert` 后，匹配已启用的 `NotificationRule`（按指标/地点/级别）。
  - 发送：对匹配规则的每个渠道（SMS/PUSH）创建一条 `NotificationLog` 并发送；失败按指数退避重试至配置上限（`notification.max-retries`）。
  - 降级：当 `notification.enabled=false` 或未配置网关时，发送逻辑记录日志并视为 no-op 成功，业务流不回滚。
  - 审计：所有发送尝试写入审计服务（动作、规则ID、渠道、结果），满足 4A 可追溯要求。
