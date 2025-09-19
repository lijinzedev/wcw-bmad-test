-- 开启UUID扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 组织单位表
CREATE TABLE IF NOT EXISTS organizations (
    organization_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- MINE, COMPANY, GROUP
    parent_id UUID REFERENCES organizations(organization_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
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
CREATE TABLE IF NOT EXISTS roles (
    role_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_name VARCHAR(100) NOT NULL UNIQUE,
    permissions JSONB -- 存储权限列表
);

-- 用户与角色的中间表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(user_id),
    role_id UUID NOT NULL REFERENCES roles(role_id),
    PRIMARY KEY (user_id, role_id)
);

-- 风险表
CREATE TABLE IF NOT EXISTS risks (
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
CREATE INDEX IF NOT EXISTS idx_risks_level ON risks(level);
CREATE INDEX IF NOT EXISTS idx_risks_responsible_org ON risks(responsible_org_id);

-- 隐患表
CREATE TABLE IF NOT EXISTS hazards (
    hazard_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    level VARCHAR(50),
    location VARCHAR(255),
    reporter_id UUID NOT NULL REFERENCES users(user_id),
    reported_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    rectification_deadline DATE,
    rectifier_id UUID REFERENCES users(user_id),
    verifier_id UUID REFERENCES users(user_id),
    risk_id UUID REFERENCES risks(risk_id),
    gov_flag BOOLEAN DEFAULT FALSE,
    gov_source VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_hazards_status ON hazards(status);
CREATE INDEX IF NOT EXISTS idx_hazards_reporter_id ON hazards(reporter_id);

-- 隐患更新记录表
CREATE TABLE IF NOT EXISTS hazard_updates (
    update_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hazard_id UUID NOT NULL REFERENCES hazards(hazard_id),
    operator_id UUID NOT NULL REFERENCES users(user_id),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    attachments JSONB
);

-- 不安全行为表
CREATE TABLE IF NOT EXISTS behaviors (
    behavior_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    occurred_at TIMESTAMPTZ NOT NULL,
    location VARCHAR(255),
    person_id UUID,
    person_name VARCHAR(255),
    behavior_type VARCHAR(100),
    description TEXT,
    rule_violated VARCHAR(255),
    action_taken TEXT,
    handler_id UUID,
    handler_name VARCHAR(255),
    handled_at TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_behaviors_person ON behaviors(person_name);
CREATE INDEX IF NOT EXISTS idx_behaviors_type ON behaviors(behavior_type);
CREATE INDEX IF NOT EXISTS idx_behaviors_status ON behaviors(status);

-- 不安全行为操作记录
CREATE TABLE IF NOT EXISTS behavior_actions (
    action_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    behavior_id UUID NOT NULL REFERENCES behaviors(behavior_id),
    operator_id UUID,
    operator_name VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    attachments JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 检查计划表
CREATE TABLE IF NOT EXISTS inspection_plans (
    plan_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    level VARCHAR(50) NOT NULL,
    mine_id UUID,
    mine_name VARCHAR(255),
    scope TEXT,
    start_at TIMESTAMPTZ,
    end_at TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_inspections_mine ON inspection_plans(mine_name);
CREATE INDEX IF NOT EXISTS idx_inspections_status ON inspection_plans(status);

-- 检查执行记录表
CREATE TABLE IF NOT EXISTS inspection_records (
    record_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plan_id UUID NOT NULL REFERENCES inspection_plans(plan_id) ON DELETE CASCADE,
    item TEXT NOT NULL,
    result VARCHAR(100) NOT NULL,
    remarks TEXT,
    attachments JSONB,
    hazard_id UUID,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_inspection_records_plan ON inspection_records(plan_id);


-- 红黄牌规则表
CREATE TABLE IF NOT EXISTS disciplinary_rules (
    rule_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    organization_level VARCHAR(50) NOT NULL,
    metric_type VARCHAR(100) NOT NULL,
    threshold_window_days INTEGER NOT NULL,
    threshold_value INTEGER NOT NULL,
    severity VARCHAR(20) NOT NULL,
    notification_channels VARCHAR(255),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_discipline_rules_active ON disciplinary_rules(active);

-- 红黄牌记录表
CREATE TABLE IF NOT EXISTS disciplinary_flags (
    flag_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(organization_id),
    organization_name VARCHAR(255),
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    auto_generated BOOLEAN NOT NULL DEFAULT FALSE,
    auto_generated_at TIMESTAMPTZ,
    rule_id UUID REFERENCES disciplinary_rules(rule_id),
    rule_name VARCHAR(255),
    reason TEXT,
    deadline DATE,
    resolution_note TEXT,
    resolved_at TIMESTAMPTZ,
    created_by UUID REFERENCES users(user_id),
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_discipline_flags_org ON disciplinary_flags(organization_id);
CREATE INDEX IF NOT EXISTS idx_discipline_flags_status ON disciplinary_flags(status);

-- 安全绩效考核周期表
CREATE TABLE IF NOT EXISTS assessment_cycles (
    cycle_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    level VARCHAR(50) NOT NULL,
    start_at TIMESTAMPTZ,
    end_at TIMESTAMPTZ,
    status VARCHAR(50),
    notes TEXT,
    created_by UUID REFERENCES users(user_id),
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_assessment_cycles_level ON assessment_cycles(level);
CREATE INDEX IF NOT EXISTS idx_assessment_cycles_period ON assessment_cycles(start_at, end_at);

-- 安全绩效考核指标表
CREATE TABLE IF NOT EXISTS assessment_indicators (
    indicator_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cycle_id UUID NOT NULL REFERENCES assessment_cycles(cycle_id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    data_source VARCHAR(100),
    weight NUMERIC(10,4) NOT NULL,
    threshold_value NUMERIC(14,4),
    higher_better BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_assessment_indicator UNIQUE (cycle_id, code)
);
CREATE INDEX IF NOT EXISTS idx_assessment_indicator_cycle ON assessment_indicators(cycle_id);

-- 安全绩效考核结果表
CREATE TABLE IF NOT EXISTS assessment_results (
    result_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cycle_id UUID NOT NULL REFERENCES assessment_cycles(cycle_id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES organizations(organization_id),
    organization_name VARCHAR(255),
    score NUMERIC(14,4) NOT NULL,
    rank_order INTEGER,
    calculated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_assessment_result_cycle ON assessment_results(cycle_id);
CREATE INDEX IF NOT EXISTS idx_assessment_result_rank ON assessment_results(cycle_id, rank_order);

-- 安全绩效考核指标明细表
CREATE TABLE IF NOT EXISTS assessment_details (
    detail_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    result_id UUID NOT NULL REFERENCES assessment_results(result_id) ON DELETE CASCADE,
    indicator_id UUID NOT NULL REFERENCES assessment_indicators(indicator_id),
    indicator_code VARCHAR(100) NOT NULL,
    indicator_name VARCHAR(255) NOT NULL,
    raw_value NUMERIC(14,4),
    weighted_score NUMERIC(14,4),
    max_score NUMERIC(14,4),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_assessment_detail_result ON assessment_details(result_id);

-- 安全事故记录表
CREATE TABLE IF NOT EXISTS accidents (
    accident_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255),
    occurred_at TIMESTAMPTZ NOT NULL,
    location VARCHAR(255),
    organization_id UUID REFERENCES organizations(organization_id),
    organization_name VARCHAR(255),
    accident_type VARCHAR(100),
    severity VARCHAR(50),
    fatality_count INTEGER,
    injury_count INTEGER,
    casualty_summary TEXT,
    economic_loss NUMERIC(14,2),
    description TEXT,
    status VARCHAR(50),
    attachments TEXT,
    reporter_id UUID REFERENCES users(user_id),
    reporter_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_accidents_occurred_at ON accidents(occurred_at);
CREATE INDEX IF NOT EXISTS idx_accidents_org ON accidents(organization_id);
CREATE INDEX IF NOT EXISTS idx_accidents_type ON accidents(accident_type);

-- 安全事故与风险关联表
CREATE TABLE IF NOT EXISTS accident_risk_links (
    link_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    accident_id UUID NOT NULL REFERENCES accidents(accident_id) ON DELETE CASCADE,
    risk_id UUID NOT NULL REFERENCES risks(risk_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(user_id),
    CONSTRAINT uq_accident_risk UNIQUE (accident_id, risk_id)
);

-- 安全事故与隐患关联表
CREATE TABLE IF NOT EXISTS accident_hazard_links (
    link_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    accident_id UUID NOT NULL REFERENCES accidents(accident_id) ON DELETE CASCADE,
    hazard_id UUID NOT NULL REFERENCES hazards(hazard_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(user_id),
    CONSTRAINT uq_accident_hazard UNIQUE (accident_id, hazard_id)
);

-- 对标模板表
CREATE TABLE IF NOT EXISTS benchmark_templates (
    template_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    visibility VARCHAR(50),
    organization_level VARCHAR(50),
    default_organizations TEXT,
    created_by UUID REFERENCES users(user_id),
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_benchmark_templates_visibility ON benchmark_templates(visibility);

-- 对标模板指标配置表
CREATE TABLE IF NOT EXISTS benchmark_metric_selections (
    selection_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    template_id UUID NOT NULL REFERENCES benchmark_templates(template_id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    display_name VARCHAR(255),
    data_source VARCHAR(100),
    aggregation VARCHAR(50),
    higher_better BOOLEAN NOT NULL DEFAULT TRUE,
    weight NUMERIC(12,4),
    sort_order INTEGER,
    extra_config TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_benchmark_metric_code UNIQUE (template_id, code)
);
CREATE INDEX IF NOT EXISTS idx_benchmark_metric_template ON benchmark_metric_selections(template_id);

-- 对标执行快照表
CREATE TABLE IF NOT EXISTS benchmark_executions (
    execution_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    template_id UUID REFERENCES benchmark_templates(template_id) ON DELETE CASCADE,
    parameters_hash VARCHAR(128) NOT NULL,
    start_at TIMESTAMPTZ,
    end_at TIMESTAMPTZ,
    organization_ids TEXT,
    metric_codes TEXT,
    payload TEXT,
    generated_by UUID REFERENCES users(user_id),
    generated_by_name VARCHAR(255),
    executed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_benchmark_execution UNIQUE (template_id, parameters_hash)
);
CREATE INDEX IF NOT EXISTS idx_benchmark_execution_template ON benchmark_executions(template_id);

-- 职业健康危害因素
CREATE TABLE IF NOT EXISTS health_hazard_factors (
    factor_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    assessment_method VARCHAR(100),
    limit_value NUMERIC(10,2),
    limit_unit VARCHAR(50),
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 接害人员档案
CREATE TABLE IF NOT EXISTS health_exposure_records (
    exposure_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    factor_id UUID REFERENCES health_hazard_factors(factor_id),
    factor_name VARCHAR(255),
    employee_id UUID,
    employee_name VARCHAR(255),
    organization_id UUID,
    organization_name VARCHAR(255),
    position_title VARCHAR(255),
    start_date DATE,
    end_date DATE,
    exposure_hours_per_week INTEGER,
    protective_equipment TEXT,
    notes TEXT,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_health_exposure_factor ON health_exposure_records(factor_id);
CREATE INDEX IF NOT EXISTS idx_health_exposure_org ON health_exposure_records(organization_id);

-- 职业健康体检记录
CREATE TABLE IF NOT EXISTS health_check_records (
    check_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exposure_id UUID REFERENCES health_exposure_records(exposure_id) ON DELETE SET NULL,
    employee_id UUID,
    employee_name VARCHAR(255),
    check_type VARCHAR(100),
    check_date DATE NOT NULL,
    medical_conclusion VARCHAR(255),
    doctor_name VARCHAR(255),
    attachments TEXT,
    follow_up_needed BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_reason VARCHAR(255),
    next_check_date DATE,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_health_check_followup ON health_check_records(follow_up_needed, next_check_date);

-- 职业病病例档案
CREATE TABLE IF NOT EXISTS health_cases (
    case_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID,
    employee_name VARCHAR(255),
    organization_id UUID,
    diagnosis VARCHAR(255),
    diagnosis_date DATE,
    status VARCHAR(100),
    notes TEXT,
    attachments TEXT,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_health_cases_org ON health_cases(organization_id);

-- 监控阈值配置
CREATE TABLE IF NOT EXISTS monitoring_thresholds (
    threshold_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_code VARCHAR(100) NOT NULL,
    metric_name VARCHAR(255),
    comparison_operator VARCHAR(50) NOT NULL,
    threshold_value NUMERIC(18,4) NOT NULL,
    unit VARCHAR(50),
    severity VARCHAR(50),
    location_pattern VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_monitoring_threshold UNIQUE (metric_code, location_pattern)
);
CREATE INDEX IF NOT EXISTS idx_monitoring_threshold_metric ON monitoring_thresholds(metric_code);

-- 监控预警记录
CREATE TABLE IF NOT EXISTS monitoring_alerts (
    alert_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    threshold_id UUID REFERENCES monitoring_thresholds(threshold_id) ON DELETE SET NULL,
    external_event_id VARCHAR(100),
    metric_code VARCHAR(100) NOT NULL,
    metric_name VARCHAR(255),
    measured_value NUMERIC(18,4) NOT NULL,
    unit VARCHAR(50),
    location VARCHAR(255),
    occurred_at TIMESTAMPTZ NOT NULL,
    severity VARCHAR(50),
    message TEXT,
    raw_payload TEXT,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_monitoring_alert_external UNIQUE (external_event_id)
);
CREATE INDEX IF NOT EXISTS idx_monitoring_alert_metric ON monitoring_alerts(metric_code);
CREATE INDEX IF NOT EXISTS idx_monitoring_alert_time ON monitoring_alerts(occurred_at);

-- 监控轮询日志
CREATE TABLE IF NOT EXISTS monitoring_poll_runs (
    poll_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    message TEXT
);

