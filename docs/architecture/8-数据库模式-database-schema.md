# 8. 数据库模式 (Database Schema)

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

> 监控与通知相关新增表（示意DDL）

```
-- 监控阈值规则
CREATE TABLE monitoring_thresholds (
    threshold_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_code VARCHAR(100) NOT NULL,
    metric_name VARCHAR(255),
    comparison_operator VARCHAR(32) NOT NULL, -- >,>=,<,<=,== 或语义枚举
    threshold_value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(32),
    severity VARCHAR(32),
    location_pattern VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_mt_metric ON monitoring_thresholds(metric_code);

-- 监控预警记录
CREATE TABLE monitoring_alerts (
    alert_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    threshold_id UUID REFERENCES monitoring_thresholds(threshold_id),
    external_event_id VARCHAR(100),
    metric_code VARCHAR(100) NOT NULL,
    metric_name VARCHAR(255),
    measured_value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(32),
    location VARCHAR(255),
    occurred_at TIMESTAMPTZ NOT NULL,
    severity VARCHAR(32),
    message TEXT,
    raw_payload TEXT,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_ma_occurred ON monitoring_alerts(occurred_at DESC);
CREATE UNIQUE INDEX uq_ma_external ON monitoring_alerts(external_event_id) WHERE external_event_id IS NOT NULL;

-- 监控轮询运行记录
CREATE TABLE monitoring_poll_runs (
    poll_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    status VARCHAR(16) NOT NULL,
    message TEXT
);

-- 通知规则
CREATE TABLE notification_rules (
    rule_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_code VARCHAR(100) NOT NULL,
    location_pattern VARCHAR(255),
    severity VARCHAR(32),
    channels VARCHAR(255), -- 逗号分隔: SMS,PUSH
    recipients VARCHAR(512), -- 逗号分隔
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_nr_metric ON notification_rules(metric_code);

-- 通知发送记录
CREATE TABLE notification_logs (
    log_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rule_id UUID REFERENCES notification_rules(rule_id),
    event_type VARCHAR(64) NOT NULL,
    event_id VARCHAR(100),
    channel VARCHAR(16) NOT NULL,
    recipients TEXT,
    subject VARCHAR(255),
    message TEXT,
    status VARCHAR(16) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    error TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);
CREATE INDEX idx_nl_created ON notification_logs(created_at DESC);
```
