-- H2-compatible schema for tests

CREATE TABLE IF NOT EXISTS organizations (
    organization_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    parent_id UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    employee_id VARCHAR(100),
    organization_id UUID NOT NULL,
    is_enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    role_id UUID PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL UNIQUE,
    permissions VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_org FOREIGN KEY (organization_id) REFERENCES organizations(organization_id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(user_id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id);

-- 风险表 (H2)
CREATE TABLE IF NOT EXISTS risks (
    risk_id UUID PRIMARY KEY,
    description TEXT NOT NULL,
    category VARCHAR(100),
    location VARCHAR(255),
    level VARCHAR(50),
    control_measures TEXT,
    responsible_org_id UUID,
    responsible_user_id UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hazards (
    hazard_id UUID PRIMARY KEY,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    level VARCHAR(50),
    location VARCHAR(255),
    reporter_id UUID NOT NULL,
    reported_at TIMESTAMP,
    rectification_deadline DATE,
    rectifier_id UUID,
    verifier_id UUID,
    risk_id UUID,
    gov_flag BOOLEAN,
    gov_source VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hazard_updates (
    update_id UUID PRIMARY KEY,
    hazard_id UUID NOT NULL,
    operator_id UUID NOT NULL,
    timestamp TIMESTAMP,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    attachments CLOB
);

ALTER TABLE hazards
    ADD CONSTRAINT fk_hazards_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id);

ALTER TABLE hazard_updates
    ADD CONSTRAINT fk_hazard_updates_hazard FOREIGN KEY (hazard_id) REFERENCES hazards(hazard_id);

ALTER TABLE hazard_updates
    ADD CONSTRAINT fk_hazard_updates_operator FOREIGN KEY (operator_id) REFERENCES users(user_id);

ALTER TABLE hazards
    ADD CONSTRAINT fk_hazards_risk FOREIGN KEY (risk_id) REFERENCES risks(risk_id);

ALTER TABLE hazards
    ADD CONSTRAINT fk_hazards_rectifier FOREIGN KEY (rectifier_id) REFERENCES users(user_id);

ALTER TABLE hazards
    ADD CONSTRAINT fk_hazards_verifier FOREIGN KEY (verifier_id) REFERENCES users(user_id);

CREATE TABLE IF NOT EXISTS behaviors (
    behavior_id UUID PRIMARY KEY,
    occurred_at TIMESTAMP,
    location VARCHAR(255),
    person_id UUID,
    person_name VARCHAR(255),
    behavior_type VARCHAR(100),
    description TEXT,
    rule_violated VARCHAR(255),
    action_taken TEXT,
    handler_id UUID,
    handler_name VARCHAR(255),
    handled_at TIMESTAMP,
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS behavior_actions (
    action_id UUID PRIMARY KEY,
    behavior_id UUID NOT NULL,
    operator_id UUID,
    operator_name VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    attachments CLOB,
    created_at TIMESTAMP
);

ALTER TABLE behavior_actions
    ADD CONSTRAINT fk_behavior_actions_behavior FOREIGN KEY (behavior_id) REFERENCES behaviors(behavior_id);

CREATE TABLE IF NOT EXISTS inspection_plans (
    plan_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    level VARCHAR(50) NOT NULL,
    mine_id UUID,
    mine_name VARCHAR(255),
    scope TEXT,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inspection_records (
    record_id UUID PRIMARY KEY,
    plan_id UUID NOT NULL,
    item TEXT NOT NULL,
    result VARCHAR(100) NOT NULL,
    remarks TEXT,
    attachments CLOB,
    hazard_id UUID,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP
);

ALTER TABLE inspection_records
    ADD CONSTRAINT fk_inspection_records_plan FOREIGN KEY (plan_id) REFERENCES inspection_plans(plan_id);


CREATE TABLE IF NOT EXISTS disciplinary_rules (
    rule_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    organization_level VARCHAR(50) NOT NULL,
    metric_type VARCHAR(100) NOT NULL,
    threshold_window_days INTEGER NOT NULL,
    threshold_value INTEGER NOT NULL,
    severity VARCHAR(20) NOT NULL,
    notification_channels VARCHAR(255),
    notes CLOB,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS disciplinary_flags (
    flag_id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    organization_name VARCHAR(255),
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    auto_generated BOOLEAN NOT NULL,
    auto_generated_at TIMESTAMP,
    rule_id UUID,
    rule_name VARCHAR(255),
    reason CLOB,
    deadline DATE,
    resolution_note CLOB,
    resolved_at TIMESTAMP,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE disciplinary_rules
    ADD CONSTRAINT fk_discipline_rules_active CHECK (active IN (TRUE, FALSE));

ALTER TABLE disciplinary_flags
    ADD CONSTRAINT fk_discipline_flags_org FOREIGN KEY (organization_id) REFERENCES organizations(organization_id);

ALTER TABLE disciplinary_flags
    ADD CONSTRAINT fk_discipline_flags_rule FOREIGN KEY (rule_id) REFERENCES disciplinary_rules(rule_id);

ALTER TABLE disciplinary_flags
    ADD CONSTRAINT fk_discipline_flags_creator FOREIGN KEY (created_by) REFERENCES users(user_id);


CREATE TABLE IF NOT EXISTS assessment_cycles (
    cycle_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    level VARCHAR(50) NOT NULL,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    status VARCHAR(50),
    notes CLOB,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assessment_indicators (
    indicator_id UUID PRIMARY KEY,
    cycle_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    data_source VARCHAR(100),
    weight DOUBLE,
    threshold_value DOUBLE,
    higher_better BOOLEAN,
    description CLOB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE assessment_indicators
    ADD CONSTRAINT fk_assessment_indicator_cycle FOREIGN KEY (cycle_id) REFERENCES assessment_cycles(cycle_id) ON DELETE CASCADE;

CREATE UNIQUE INDEX idx_unique_indicator_code ON assessment_indicators(cycle_id, code);

CREATE TABLE IF NOT EXISTS assessment_results (
    result_id UUID PRIMARY KEY,
    cycle_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    organization_name VARCHAR(255),
    score DOUBLE,
    rank_order INTEGER,
    calculated_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE assessment_results
    ADD CONSTRAINT fk_assessment_result_cycle FOREIGN KEY (cycle_id) REFERENCES assessment_cycles(cycle_id) ON DELETE CASCADE;

ALTER TABLE assessment_results
    ADD CONSTRAINT fk_assessment_result_org FOREIGN KEY (organization_id) REFERENCES organizations(organization_id);

CREATE TABLE IF NOT EXISTS assessment_details (
    detail_id UUID PRIMARY KEY,
    result_id UUID NOT NULL,
    indicator_id UUID NOT NULL,
    indicator_code VARCHAR(100) NOT NULL,
    indicator_name VARCHAR(255) NOT NULL,
    raw_value DOUBLE,
    weighted_score DOUBLE,
    max_score DOUBLE,
    notes CLOB,
    created_at TIMESTAMP
);

ALTER TABLE assessment_details
    ADD CONSTRAINT fk_assessment_detail_result FOREIGN KEY (result_id) REFERENCES assessment_results(result_id) ON DELETE CASCADE;

ALTER TABLE assessment_details
    ADD CONSTRAINT fk_assessment_detail_indicator FOREIGN KEY (indicator_id) REFERENCES assessment_indicators(indicator_id);

CREATE TABLE IF NOT EXISTS accidents (
    accident_id UUID PRIMARY KEY,
    title VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL,
    location VARCHAR(255),
    organization_id UUID,
    organization_name VARCHAR(255),
    accident_type VARCHAR(100),
    severity VARCHAR(50),
    fatality_count INTEGER,
    injury_count INTEGER,
    casualty_summary CLOB,
    economic_loss DECIMAL(14,2),
    description CLOB,
    status VARCHAR(50),
    attachments CLOB,
    reporter_id UUID,
    reporter_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accident_risk_links (
    link_id UUID PRIMARY KEY,
    accident_id UUID NOT NULL,
    risk_id UUID NOT NULL,
    created_at TIMESTAMP,
    created_by UUID
);

CREATE TABLE IF NOT EXISTS accident_hazard_links (
    link_id UUID PRIMARY KEY,
    accident_id UUID NOT NULL,
    hazard_id UUID NOT NULL,
    created_at TIMESTAMP,
    created_by UUID
);

ALTER TABLE accidents
    ADD CONSTRAINT fk_accidents_org FOREIGN KEY (organization_id) REFERENCES organizations(organization_id);

ALTER TABLE accidents
    ADD CONSTRAINT fk_accidents_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id);

ALTER TABLE accident_risk_links
    ADD CONSTRAINT fk_accident_risk_accident FOREIGN KEY (accident_id) REFERENCES accidents(accident_id) ON DELETE CASCADE;

ALTER TABLE accident_risk_links
    ADD CONSTRAINT fk_accident_risk_risk FOREIGN KEY (risk_id) REFERENCES risks(risk_id);

ALTER TABLE accident_risk_links
    ADD CONSTRAINT uq_accident_risk UNIQUE (accident_id, risk_id);

ALTER TABLE accident_hazard_links
    ADD CONSTRAINT fk_accident_hazard_accident FOREIGN KEY (accident_id) REFERENCES accidents(accident_id) ON DELETE CASCADE;

ALTER TABLE accident_hazard_links
    ADD CONSTRAINT fk_accident_hazard_hazard FOREIGN KEY (hazard_id) REFERENCES hazards(hazard_id);

ALTER TABLE accident_hazard_links
    ADD CONSTRAINT uq_accident_hazard UNIQUE (accident_id, hazard_id);


CREATE TABLE IF NOT EXISTS benchmark_templates (
    template_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description CLOB,
    visibility VARCHAR(50),
    organization_level VARCHAR(50),
    default_organizations CLOB,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS benchmark_metric_selections (
    selection_id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    display_name VARCHAR(255),
    data_source VARCHAR(100),
    aggregation VARCHAR(50),
    higher_better BOOLEAN,
    weight DOUBLE,
    sort_order INTEGER,
    extra_config CLOB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE benchmark_metric_selections
    ADD CONSTRAINT fk_benchmark_metric_template FOREIGN KEY (template_id) REFERENCES benchmark_templates(template_id) ON DELETE CASCADE;

CREATE UNIQUE INDEX idx_benchmark_metric_code ON benchmark_metric_selections(template_id, code);

CREATE TABLE IF NOT EXISTS benchmark_executions (
    execution_id UUID PRIMARY KEY,
    template_id UUID,
    parameters_hash VARCHAR(128) NOT NULL,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    organization_ids CLOB,
    metric_codes CLOB,
    payload CLOB,
    generated_by UUID,
    generated_by_name VARCHAR(255),
    executed_at TIMESTAMP
);

ALTER TABLE benchmark_executions
    ADD CONSTRAINT fk_benchmark_execution_template FOREIGN KEY (template_id) REFERENCES benchmark_templates(template_id) ON DELETE CASCADE;

CREATE UNIQUE INDEX idx_benchmark_execution_hash ON benchmark_executions(template_id, parameters_hash);

-- 职业健康危害因素
CREATE TABLE IF NOT EXISTS health_hazard_factors (
    factor_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    description CLOB,
    assessment_method VARCHAR(100),
    limit_value DECIMAL(10,2),
    limit_unit VARCHAR(50),
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 接害人员档案
CREATE TABLE IF NOT EXISTS health_exposure_records (
    exposure_id UUID PRIMARY KEY,
    factor_id UUID,
    factor_name VARCHAR(255),
    employee_id UUID,
    employee_name VARCHAR(255),
    organization_id UUID,
    organization_name VARCHAR(255),
    position_title VARCHAR(255),
    start_date DATE,
    end_date DATE,
    exposure_hours_per_week INTEGER,
    protective_equipment CLOB,
    notes CLOB,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_health_exposure_factor ON health_exposure_records(factor_id);
CREATE INDEX IF NOT EXISTS idx_health_exposure_org ON health_exposure_records(organization_id);

-- 职业健康体检记录
CREATE TABLE IF NOT EXISTS health_check_records (
    check_id UUID PRIMARY KEY,
    exposure_id UUID,
    employee_id UUID,
    employee_name VARCHAR(255),
    check_type VARCHAR(100),
    check_date DATE,
    medical_conclusion VARCHAR(255),
    doctor_name VARCHAR(255),
    attachments CLOB,
    follow_up_needed BOOLEAN,
    follow_up_reason VARCHAR(255),
    next_check_date DATE,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_health_check_followup ON health_check_records(follow_up_needed, next_check_date);

-- 职业病病例档案
CREATE TABLE IF NOT EXISTS health_cases (
    case_id UUID PRIMARY KEY,
    employee_id UUID,
    employee_name VARCHAR(255),
    organization_id UUID,
    diagnosis VARCHAR(255),
    diagnosis_date DATE,
    status VARCHAR(100),
    notes CLOB,
    attachments CLOB,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_health_cases_org ON health_cases(organization_id);

-- 监控阈值配置
CREATE TABLE IF NOT EXISTS monitoring_thresholds (
    threshold_id UUID PRIMARY KEY,
    metric_code VARCHAR(100) NOT NULL,
    metric_name VARCHAR(255),
    comparison_operator VARCHAR(50) NOT NULL,
    threshold_value DECIMAL(18,4) NOT NULL,
    unit VARCHAR(50),
    severity VARCHAR(50),
    location_pattern VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_by_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_monitoring_threshold_metric ON monitoring_thresholds(metric_code);

-- 监控预警记录
CREATE TABLE IF NOT EXISTS monitoring_alerts (
    alert_id UUID PRIMARY KEY,
    threshold_id UUID,
    external_event_id VARCHAR(100),
    metric_code VARCHAR(100) NOT NULL,
    metric_name VARCHAR(255),
    measured_value DECIMAL(18,4) NOT NULL,
    unit VARCHAR(50),
    location VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL,
    severity VARCHAR(50),
    message CLOB,
    raw_payload CLOB,
    acknowledged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_monitoring_alert_metric ON monitoring_alerts(metric_code);
CREATE INDEX IF NOT EXISTS idx_monitoring_alert_time ON monitoring_alerts(occurred_at);

-- 监控轮询日志
CREATE TABLE IF NOT EXISTS monitoring_poll_runs (
    poll_id UUID PRIMARY KEY,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    message CLOB
);

