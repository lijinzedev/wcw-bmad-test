# 煤矿双预防系统架构设计文档



| **日期**   | **版本** | **描述**                                                     | **作者**            |
| ---------- | -------- | ------------------------------------------------------------ | ------------------- |
| 2025-09-10 | 1.0      | 初始草案 - 定义高层架构和技术栈                              | Winston (Architect) |
| 2025-09-10 | 2.0      | 根据用户反馈更新为私有化部署的分层单体架构，并引入4A安全体系 | Winston (Architect) |
| 2025-09-10 | 2.1      | 完成架构师清单验证并添加后续步骤                             | Winston (Architect) |

## 1. 简介 (Introduction)

本文件概述了“煤矿双预防系统”的完整全栈架构，包括后端系统、前端应用（PC和移动端）及其集成方式。它将作为所有AI驱动开发工作的唯一技术事实来源，确保整个技术栈的一致性。

### 1.1 项目启动方式

- **方式:** 全新绿地项目 (Greenfield project)
- **说明:** 本项目将从零开始构建，不基于任何现有的启动模板或旧有代码库。

## 2. 高层架构 (High Level Architecture)

### 2.1 技术摘要

本系统将采用**分层的单体应用架构**，部署于企业**私有数据中心**。前端（PC Web和移动App）通过 **Nginx** 作为反向代理和API网关，与后端Java应用进行通信。后端应用内部按业务领域进行模块化分层（如表现层、业务逻辑层、数据访问层），以保证代码的清晰度和可维护性。整体架构旨在满足PRD中对四级数据贯通和私有化部署的要求。

### 2.2 平台与基础设施选择

- **平台:** **企业私有数据中心 / 本地服务器 (Private Data Center / On-Premise Servers)**
- **理由:** 满足企业数据安全和自主可控的要求，不依赖公有云服务。
- **关键组件:**
  - **计算:** 物理服务器或虚拟化平台 (如 VMware vSphere)
  - **数据库:** 自建 PostgreSQL 数据库集群
  - **存储:** 自建 S3 兼容对象存储 (如 MinIO)
  - **网络:** 企业内部网络，Nginx 作为核心网关

### 2.3 代码仓库结构

- **结构:** **Monorepo (单体仓库)**
- **理由:** 便于管理前端、后端和移动端之间的共享代码，并简化统一的CI/CD流程。
- **管理工具:** Nx。

### 2.4 高层架构图

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

### 2.5 架构与设计模式

- **分层单体架构 (Layered Monolithic):** 应用被构建为一个单一的、统一的单元，内部通过逻辑分层（表示层、业务层、持久层、数据库层）来组织代码，实现关注点分离。
- **反向代理/API网关 (Reverse Proxy/API Gateway):** 使用Nginx作为所有客户端请求的单一入口，处理SSL卸载、负载均衡、路由和静态资源服务。
- **仓库模式 (Repository Pattern):** 在服务内部，数据访问逻辑将通过仓库层进行抽象，使业务逻辑与数据存储实现分离。

### 2.6 安全架构 (4A Architecture)

为了满足企业级安全需求，系统将基于4A架构理念进行设计：

- **认证 (Authentication):** 确认用户身份。我们将使用后端框架内置的安全模块（如Spring Security）实现用户登录和身份验证。
- **授权 (Authorization):** 管理用户权限。我们将基于PRD中定义的四级角色模型，实现精细化的功能和数据访问控制。
- **审计 (Auditing):** 记录用户行为。所有关键操作都将被详细记录到日志中，以便进行安全审计和事后追溯。
- **授权管理 (Administration):** 提供后台管理功能，允许系统管理员对用户、角色和权限进行配置。

## 3. 技术栈 (Tech Stack)

### 3.1 技术栈表格

| **类别**            | **技术**          | **版本**     | **用途**                  | **理由**                                                     |
| ------------------- | ----------------- | ------------ | ------------------------- | ------------------------------------------------------------ |
| **后端语言**        | Java              | 17 (LTS)     | 应用核心开发              | 生态成熟，性能稳定，企业级应用首选，LTS版本保障长期支持。    |
| **后端框架**        | Spring Boot 3.x   | 3.1.5        | 快速构建应用              | 业界标准，简化Java开发，提供强大的Web和数据访问能力。        |
| **前端框架 (PC)**   | Vue.js            | 3.3.4        | PC Web管理界面            | 渐进式框架，学习曲线平缓，性能优秀，适合构建复杂单页应用。   |
| **移动端框架**      | Uni-app           | 3.8.12       | iOS & Android App         | 基于Vue.js，一套代码多端发布，极大提升移动端开发效率。       |
| **数据库 (关系型)** | PostgreSQL        | 15.4         | 核心业务数据存储          | 功能强大，兼容ACID，开源且适合私有化部署。                   |
| **对象存储**        | MinIO             | LATEST       | 存储图片、视频、文档附件  | 开源，S3兼容，易于在私有数据中心部署。                       |
| **API 网关**        | Nginx             | 1.25.2       | API统一入口，反向代理     | 高性能，稳定可靠，配置灵活，是事实上的行业标准。             |
| **代码仓库托管**    | Gitee             | - (私有化)   | 源代码管理                | 满足国内企业代码托管的需求，支持私有化部署。                 |
| **Monorepo管理**    | Nx                | 16.8.1       | 管理Gitee仓库内的代码结构 | 提供强大的依赖管理、代码生成和缓存机制，优化大型Monorepo开发体验。 |
| **容器化**          | Docker            | 24.0.5       | 应用打包                  | 实现环境一致性，便于部署。                                   |
| **容器编排**        | Docker Compose    | 2.21.0       | 开发与生产环境容器管理    | 相比K8s更轻量，易于配置和管理，完全满足单体应用的部署需求。  |
| **CI/CD**           | Jenkins           | 2.414.2(LTS) | 自动化构建、测试、部署    | 开源CI/CD领导者，插件生态丰富，支持与Gitee的深度集成。       |
| **认证与授权**      | Spring Security   | 6.1.4        | 实现4A安全架构            | Spring生态原生安全框架，功能强大，能完全满足PRD定义的四级权限需求。 |
| **日志框架**        | Logback           | 1.4.11       | 应用日志，支持4A审计      | Spring Boot默认集成，性能高效，配置灵活，用于记录关键操作以备审计。 |
| **后端测试**        | JUnit 5 / Mockito | 5.9.3        | 单元/集成测试             | Java开发的标准测试框架。                                     |
| **前端测试**        | Vitest / Cypress  | 0.34 / 13.2  | 单元/E2E测试              | Vitest与Vite构建工具无缝集成，Cypress提供可靠的端到端测试。  |

## 4. 数据模型 (Data Models)

#### **User (用户)**

- **用途:** 存储所有系统用户的基本信息，用于登录和身份识别。
- **关键属性:** `userId`, `username`, `passwordHash`, `fullName`, `employeeId`, `organizationId`, `isEnabled`
- **关系:** 多对多 `Role`, 一对多 `Organization`

#### **Role (角色)**

- **用途:** 定义用户角色，用于权限控制。
- **关键属性:** `roleId`, `roleName`, `permissions`
- **关系:** 多对多 `User`

#### **Organization (组织/单位)**

- **用途:** 存储从矿井到集团的四级组织架构。
- **关键属性:** `organizationId`, `name`, `type`, `parentId`
- **关系:** 多对一 `User`

#### **Risk (风险)**

- **用途:** 存储辨识出的安全风险点信息 (对应 `FR1`)。
- **关键属性:** `riskId`, `description`, `category`, `location`, `level`, `controlMeasures`, `responsibleOrgId`, `responsibleUserId`
- **关系:** 一对多 `Hazard`

#### **Hazard (隐患)**

- **用途:** 存储隐患排查治理的全流程信息 (对应 `FR2`)。
- **关键属性:** `hazardId`, `description`, `status`, `level`, `reporterId`, `reportedAt`, `rectificationDeadline`, `rectifierId`, `verifierId`, `riskId`
- **关系:** 多对一 `Risk`

#### **HazardUpdate (隐患更新记录)**

- **用途:** 记录隐患生命周期中的每一次状态变更和操作，形成完整台账。
- **关键属性:** `updateId`, `hazardId`, `operatorId`, `timestamp`, `action`, `details`, `attachments`
- **关系:** 多对一 `Hazard`

## 5. 组件 (Components)

在我们的单体应用内部，将按照以下逻辑组件进行代码组织，以确保高内聚和低耦合。

#### **核心组件 (Core Components)**

- **`core-security` (安全与权限组件):** 负责实现4A安全架构。
- **`core-user` (用户与组织组件):** 管理用户、角色、组织等基础数据。
- **`core-common` (通用组件):** 提供全局共享的工具类、常量、DTOs等。

#### **业务组件 (Business Components)**

- **`biz-risk` (风险管理组件):** 实现风险清单相关功能 (`FR1`)。
- **`biz-hazard` (隐患管理组件):** 实现隐患闭环流程 (`FR2`)。
- **`biz-inspection` (监督检查组件):** 支持公司、集团及政府的监督检查 (`FR4`, `FR5`)。
- **`biz-behavior` (不安全行为组件):** 管理“三违”行为 (`FR3`)。
- **`biz-analysis` (数据分析与报告组件):** 实现考核、对标、事故管理等 (`FR7`, `FR8`, `FR10`)。
- **`biz-health` (职业健康组件):** 管理职业健康信息 (`FR11`)。

#### **集成组件 (Integration Components)**

- **`int-monitoring` (监控系统集成组件):** 对接外部安全监控系统 (`FR9`)。
- **`int-notification` (通知服务集成组件):** 对接外部短信或App推送网关。

## 6. 外部 API (External APIs)

本系统需要与以下外部系统进行集成。

#### **安全监控系统 API**

- **用途:** (`FR9`) 从现有的安全监控系统（如瓦斯、粉尘监测）获取实时异常数据。
- **文档:** (待提供) - 需要安全监控系统供应商提供API接口文档。
- **认证方式:** (待定) - 可能是IP白名单、API Key或OAuth2。
- **关键端点:**
  - `GET /api/v1/alerts`: 获取指定时间范围内的异常报警数据。
- **集成说明:** `int-monitoring` 组件将定期轮询此接口，获取增量报警数据，并将其转化为系统内部的预警事件。

#### **通知网关 API (短信/App推送)**

- **用途:** 发送关键事件通知，如重大隐患指派、超时未整改警告、智能预警等。
- **文档:** (待提供) - 需要企业内部的短信网关或App推送服务商提供API文档。
- **认证方式:** (待定) - 通常是基于AppKey/SecretKey的签名机制。
- **关键端点:**
  - `POST /api/v1/send_sms`: 发送短信通知。
  - `POST /api/v1/push_notification`: 推送App通知。
- **集成说明:** `int-notification` 组件将提供统一的接口，供其他业务组件调用，以发送通知。

## 7. REST API 规范 (REST API Spec)

所有提供给前端和移动端的API都将遵循RESTful设计原则，并以OpenAPI 3.0格式进行定义。

### 7.1 通用约定

- **根路径 (Base Path):** 所有API都将以 `/api/v1` 作为根路径。

- **认证 (Authentication):** 除登录接口外，所有API请求的Header中都必须包含一个有效的JWT (JSON Web Token): `Authorization: Bearer <token>`。

- **数据格式 (Data Format):** 所有请求和响应的主体都使用 `application/json` 格式。

- **错误处理 (Error Handling):** 发生错误时，API将返回相应的HTTP状态码（如400, 401, 403, 404, 500），并在响应体中包含统一的错误信息结构：

  ```
  {
    "timestamp": "2025-09-10T12:00:00.000Z",
    "status": 404,
    "error": "Not Found",
    "message": "ID为 'xxx' 的隐患未找到",
    "path": "/api/v1/hazards/xxx"
  }
  ```

- **分页 (Pagination):** 对于返回列表的GET请求，将使用基于 `page` 和 `size` 参数的分页。

### 7.2 OpenAPI 定义 (部分示例)

以下是核心认证和授权API的初步定义，用于演示格式。完整的API将在开发过程中逐步完善。

```
openapi: 3.0.0
info:
  title: 煤矿双预防系统 API
  version: "1.0.0"
  description: 用于PC端和移动端的后端API

paths:
  /api/v1/auth/login:
    post:
      summary: 用户登录
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                username:
                  type: string
                password:
                  type: string
      responses:
        '200':
          description: 登录成功
          content:
            application/json:
              schema:
                type: object
                properties:
                  token:
                    type: string
                    description: "JWT Token"
                  user:
                    $ref: '#/components/schemas/User'
        '401':
          description: 认证失败

components:
  schemas:
    User:
      type: object
      properties:
        userId:
          type: string
          format: uuid
        username:
          type: string
        fullName:
          type: string
        organizationName:
          type: string
```

## 8. 数据库模式 (Database Schema)

以下是使用PostgreSQL DDL语法定义的核心表结构。

```
-- 开启UUID扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 组织单位表
CREATE TABLE organizations (
    organization_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- MINE, COMPANY, GROUP
    parent_id UUID REFERENCES organizations(organization_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 用户表
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    employee_id VARCHAR(100) UNIQUE,
    organization_id UUID NOT NULL REFERENCES organizations(organization_id),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 角色表
CREATE TABLE roles (
    role_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_name VARCHAR(100) NOT NULL UNIQUE,
    permissions JSONB -- 存储权限列表
);

-- 用户与角色的中间表
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(user_id),
    role_id UUID NOT NULL REFERENCES roles(role_id),
    PRIMARY KEY (user_id, role_id)
);

-- 风险表
CREATE TABLE risks (
    risk_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    description TEXT NOT NULL,
    category VARCHAR(100),
    location VARCHAR(255),
    level VARCHAR(50), -- 重大, 较大, 一般, 低
    control_measures TEXT,
    responsible_org_id UUID REFERENCES organizations(organization_id),
    responsible_user_id UUID REFERENCES users(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 隐患表
CREATE TABLE hazards (
    hazard_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL, -- 待指派, 整改中, 待验收, 已关闭, 已作废
    level VARCHAR(50), -- 一般, 重大
    reporter_id UUID NOT NULL REFERENCES users(user_id),
    reported_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    rectification_deadline DATE,
    rectifier_id UUID REFERENCES users(user_id),
    verifier_id UUID REFERENCES users(user_id),
    risk_id UUID REFERENCES risks(risk_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
-- 为常用查询字段添加索引
CREATE INDEX idx_hazards_status ON hazards(status);
CREATE INDEX idx_hazards_reporter_id ON hazards(reporter_id);
CREATE INDEX idx_hazards_rectifier_id ON hazards(rectifier_id);


-- 隐患更新记录表
CREATE TABLE hazard_updates (
    update_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hazard_id UUID NOT NULL REFERENCES hazards(hazard_id),
    operator_id UUID NOT NULL REFERENCES users(user_id),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    attachments JSONB -- 存储文件URL等信息
);
```

## 9. 项目源代码树结构 (Source Tree)

这是我们基于Nx Monorepo的最佳实践设计的项目文件夹结构。

```
/
├── apps/                           # 存放可独立部署的应用
│   ├── backend/                    # 后端Java Spring Boot应用
│   │   ├── src/main/java/com/shanergy/bprev/
│   │   │   ├── BprevApplication.java
│   │   │   ├── common/             # 对应 core-common
│   │   │   ├── config/             # 应用配置
│   │   │   ├── controller/         # API控制器 (表现层)
│   │   │   ├── model/              # 数据实体
│   │   │   ├── repository/         # 数据仓库 (持久层)
│   │   │   ├── security/           # 对应 core-security
│   │   │   └── service/            # 业务逻辑层 (存放各业务组件实现)
│   │   └── pom.xml
│   ├── frontend-pc/                # 前端PC Vue.js应用
│   │   ├── src/
│   │   │   ├── assets/
│   │   │   ├── components/
│   │   │   ├── router/
│   │   │   ├── stores/
│   │   │   ├── views/
│   │   │   └── main.js
│   │   └── package.json
│   └── frontend-mobile/            # 前端移动端 Uni-app应用
│       ├── src/
│       │   ├── pages/
│       │   ├── static/
│       │   └── main.js
│       └── package.json
│
├── libs/                           # 存放共享库和业务逻辑模块
│   └── shared-types/               # 前后端共享的TypeScript类型定义
│       └── src/
│           ├── index.ts
│           └── lib/
│
├── tools/                          # 存放构建、部署等脚本
│   ├── jenkins/
│   │   └── Jenkinsfile             # Jenkins CI/CD流水线定义
│   └── scripts/
│
├── docs/                           # 存放所有项目文档
│   ├── prd.md
│   └── architecture.md
│
├── nx.json                         # Nx Monorepo 配置文件
├── package.json                    # 根项目的package.json
└── README.md
```

## 10. 清单检查结果报告 (Checklist Results Report)

### 摘要

- **总体架构准备情况:** **高**
- **项目类型:** 全栈 (Full-stack) - 已评估所有相关部分。
- **关键风险:**
  1. **外部系统集成不确定性:** 安全监控系统和通知网关的API尚未提供，可能导致集成阶段的延误和返工。
  2. **私有化部署复杂性:** 相比公有云，私有化部署对运维团队要求更高，需要确保硬件资源、网络配置和持续维护的能力。
- **架构关键优势:**
  - **清晰明确:** 采用了行业标准的分层单体架构，逻辑清晰，易于理解和开发。
  - **技术选型稳健:** 所选技术均为成熟、稳定的开源方案，社区支持良好，便于团队招聘和长期维护。
  - **文档完整性高:** 从PRD到架构设计，文档链条完整，为开发提供了坚实的基础。

### 分项评估

| **类别**              | **状态**   | **关键发现**                                                 |
| --------------------- | ---------- | ------------------------------------------------------------ |
| **1. 需求对齐**       | ✅ 通过     | 架构设计完整覆盖了PRD中定义的所有功能性和非功能性需求。      |
| **2. 架构基础**       | ✅ 通过     | 分层架构和设计模式选择合理，职责划分清晰。                   |
| **3. 技术栈与决策**   | ✅ 通过     | 技术选型具体，版本明确，并有充分的理由支撑。                 |
| **4. 前端设计与实现** | ✅ 通过     | 源代码树结构为前端PC和移动端提供了清晰的组织结构。           |
| **5. 韧性与运维**     | ⚠️ 部分通过 | **建议:** 需进一步明确日志审计的具体内容和格式，并制定详细的数据库备份与恢复策略。 |
| **6. 安全与合规**     | ✅ 通过     | 4A安全架构提供了坚实的安全基础。                             |
| **7. 实现指导**       | ✅ 通过     | 源代码树、组件划分和API规范为开发提供了清晰的指导。          |
| **8. 依赖与集成**     | ⚠️ 部分通过 | **风险:** 外部API依赖是当前最大的不确定因素。                |
| **9. AI Agent适用性** | ✅ 通过     | 结构清晰，模式统一，非常适合AI Agent进行后续的代码生成工作。 |

### 风险评估与建议

- **最高优先级风险:** **外部API集成**
  - **建议:** **立即**与安全监控系统和通知网关的提供方联系，获取API文档和测试账号。在开发早期建立技术预研（Spike）任务，验证接口的可行性。
- **次高优先级风险:** **运维能力**
  - **建议:** 在开发的同时，运维团队应开始准备服务器、网络、数据库和MinIO的部署方案，并进行性能测试。

### 最终决定

- **准备就绪，可交付给产品负责人(PO):** 架构文档已足够全面和稳健，可以作为后续项目规划和开发工作的技术基石。建议在解决上述风险的同时，项目可以进入下一阶段。

## 11. 后续步骤 (Next Steps)

### 产品负责人 (Product Owner - PO) 指令

> “架构师Winston已完成系统架构设计并进行了验证。请您基于这份最终的《系统架构设计文档》和《产品需求文档(PRD)》，运行您的 **“PO大师清单”**，从产品和业务角度对整体计划进行最终审核，确保技术实现与业务目标完全对齐，并验证故事的顺序和依赖关系是否合理。这是我们进入开发前的最后一道质量门。”

### 开发团队 (Development Team) 指令

> “这是项目的最终技术蓝图。请所有开发人员（包括AI Agents）仔细阅读这份文档。在PO完成最终审核后，我们将基于PR`docs/prd.md`中的**Epic 1**和本文档中的**源代码树结构**及**API规范**，开始项目的初始化和第一个用户故事的开发工作。”