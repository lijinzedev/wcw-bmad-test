# 4. 数据模型 (Data Models)

### **User (用户)**

- **用途:** 存储所有系统用户的基本信息，用于登录和身份识别。
- **关键属性:** `userId`, `username`, `passwordHash`, `fullName`, `employeeId`, `organizationId`, `isEnabled`
- **关系:** 多对多 `Role`, 一对多 `Organization`

### **Role (角色)**

- **用途:** 定义用户角色，用于权限控制。
- **关键属性:** `roleId`, `roleName`, `permissions`
- **关系:** 多对多 `User`

### **Organization (组织/单位)**

- **用途:** 存储从矿井到集团的四级组织架构。
- **关键属性:** `organizationId`, `name`, `type`, `parentId`
- **关系:** 多对一 `User`

### **Risk (风险)**

- **用途:** 存储辨识出的安全风险点信息 (对应 `FR1`)。
- **关键属性:** `riskId`, `description`, `category`, `location`, `level`, `controlMeasures`, `responsibleOrgId`, `responsibleUserId`
- **关系:** 一对多 `Hazard`

### **Hazard (隐患)**

- **用途:** 存储隐患排查治理的全流程信息 (对应 `FR2`)。
- **关键属性:** `hazardId`, `description`, `status`, `level`, `reporterId`, `reportedAt`, `rectificationDeadline`, `rectifierId`, `verifierId`, `riskId`
- **关系:** 多对一 `Risk`

### **HazardUpdate (隐患更新记录)**

- **用途:** 记录隐患生命周期中的每一次状态变更和操作，形成完整台账。
- **关键属性:** `updateId`, `hazardId`, `operatorId`, `timestamp`, `action`, `details`, `attachments`
- **关系:** 多对一 `Hazard`

### **MonitoringThreshold (监控阈值规则)**

- **用途:** 为监控指标（如瓦斯、粉尘等）配置触发预警的比较符与阈值，支持地点包含匹配与启停控制。
- **关键属性:** `thresholdId`, `metricCode`, `metricName`, `comparisonOperator(> >= < <= == 或语义枚举)`, `thresholdValue`, `unit`, `severity`, `locationPattern`, `enabled`, `createdBy`, `createdByName`, `createdAt`, `updatedAt`
- **关系:** 一对多 `MonitoringAlert`（通过 `thresholdId` 关联）

### **MonitoringAlert (监控预警记录)**

- **用途:** 存储命中阈值规则后生成的预警事件，包含时间、地点、测量值、原始载荷等；支持去重（`externalEventId`）。
- **关键属性:** `alertId`, `thresholdId`, `externalEventId`, `metricCode`, `metricName`, `measuredValue`, `unit`, `location`, `occurredAt`, `severity`, `message`, `rawPayload`, `acknowledged`, `createdAt`
- **关系:** 多对一 `MonitoringThreshold`

### **MonitoringPollRun (监控轮询运行记录)**

- **用途:** 记录每次外部监控数据轮询的开始/结束时间、状态与摘要，便于审计与故障排查。
- **关键属性:** `pollId`, `startedAt`, `completedAt`, `status(RUNNING/SUCCESS/FAILED)`, `message`
- **关系:** 无直接外键

### **NotificationRule (通知规则)**

- **用途:** 描述当特定监控预警出现（按 `metricCode`/`locationPattern`/`severity` 匹配）时触发的通知渠道与收件人，支持启停与测试发送。
- **关键属性:** `ruleId`, `metricCode`, `locationPattern`, `severity`, `channels(逗号分隔: SMS,PUSH)`, `recipients(逗号分隔)`, `enabled`, `createdAt`, `updatedAt`
- **关系:** 一对多 `NotificationLog`（按 `ruleId` 关联）

### **NotificationLog (通知发送记录)**

- **用途:** 记录通知发送的明细、状态与错误信息，支持失败重试与审计追踪。
- **关键属性:** `logId`, `ruleId`, `eventType(MONITORING_ALERT/TEST/...)`, `eventId`, `channel(SMS/PUSH)`, `recipients`, `subject`, `message`, `status(PENDING/SUCCESS/FAILED)`, `attempts`, `error`, `createdAt`, `completedAt`
- **关系:** 多对一 `NotificationRule`
