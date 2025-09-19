# 5. 组件 (Components)

在我们的单体应用内部，将按照以下逻辑组件进行代码组织，以确保高内聚和低耦合。

### **核心组件 (Core Components)**

- **`core-security` (安全与权限组件):** 负责实现4A安全架构。
- **`core-user` (用户与组织组件):** 管理用户、角色、组织等基础数据。
- **`core-common` (通用组件):** 提供全局共享的工具类、常量、DTOs等。

### **业务组件 (Business Components)**

- **`biz-risk` (风险管理组件):** 实现风险清单相关功能 (`FR1`)。
- **`biz-hazard` (隐患管理组件):** 实现隐患闭环流程 (`FR2`)。
- **`biz-inspection` (监督检查组件):** 支持公司、集团及政府的监督检查 (`FR4`, `FR5`)。
- **`biz-behavior` (不安全行为组件):** 管理“三违”行为 (`FR3`)。
- **`biz-analysis` (数据分析与报告组件):** 实现考核、对标、事故管理等 (`FR7`, `FR8`, `FR10`)。
- **`biz-health` (职业健康组件):** 管理职业健康信息 (`FR11`)。

### **集成组件 (Integration Components)**

- **`int-monitoring` (监控系统集成组件):** 对接外部安全监控系统 (`FR9`)。
  - 职责：基于时间窗口轮询外部 `/api/v1/alerts`，解析异常事件并写入内部预警流；提供手动触发入口与运行记录。
  - 关键类：`MonitoringClient`/`HttpMonitoringClient`、`MonitoringPoller`、`MonitoringIntegrationService`、`MonitoringService`。
  - 配置：`monitoring.enabled`、`monitoring.base-url`、`monitoring.api-key`、`monitoring.poll-delay-ms`、`monitoring.lookback-minutes`。
  - 数据：`MonitoringThreshold`（阈值规则）、`MonitoringAlert`（预警记录）、`MonitoringPollRun`（轮询运行记录）。

- **`int-notification` (通知服务集成组件):** 对接外部短信或App推送网关。
  - 职责：统一封装短信/推送发送；支持禁用模式（记录日志但不触发外呼）、失败重试与审计；对外提供通知规则与发送记录查询。
  - 关键类：`NotificationClient`/`HttpNotificationClient`、`NotificationManagerService`。
  - 配置：`notification.enabled`、`notification.base-url`、`notification.app-key`、`notification.app-secret`、`notification.max-retries`。
  - 数据：`NotificationRule`（通知规则），`NotificationLog`（发送记录）。
