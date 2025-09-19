INSERT INTO organizations (organization_id, name, type, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'Demo Mine', 'MINE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO roles (role_id, role_name, permissions)
VALUES ('22222222-2222-2222-2222-222222222222', 'ADMIN', '[]');

-- Additional USER role for authorization tests
INSERT INTO roles (role_id, role_name, permissions)
VALUES ('44444444-4444-4444-4444-444444444444', 'USER', '[]');

INSERT INTO users (user_id, username, password_hash, full_name, organization_id, is_enabled, created_at, updated_at)
VALUES ('33333333-3333-3333-3333-333333333333', 'admin', '{noop}admin123', 'Administrator', '11111111-1111-1111-1111-111111111111', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
VALUES ('33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222');

-- Non-admin user
INSERT INTO users (user_id, username, password_hash, full_name, organization_id, is_enabled, created_at, updated_at)
VALUES ('55555555-5555-5555-5555-555555555555', 'user', '{noop}user123', 'Normal User', '11111111-1111-1111-1111-111111111111', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
VALUES ('55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444');

-- Government regulator role and user
INSERT INTO roles (role_id, role_name, permissions)
VALUES ('66666666-6666-6666-6666-666666666666', 'GOVERNMENT', '[]');

INSERT INTO users (user_id, username, password_hash, full_name, organization_id, is_enabled, created_at, updated_at)
VALUES ('77777777-7777-7777-7777-777777777777', 'gov', '{noop}gov123', 'Gov Officer', '11111111-1111-1111-1111-111111111111', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
VALUES ('77777777-7777-7777-7777-777777777777', '66666666-6666-6666-6666-666666666666');

-- Sample risks for org and another org
INSERT INTO organizations (organization_id, name, type, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Other Org', 'MINE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO risks (risk_id, description, category, location, level, control_measures, responsible_org_id, responsible_user_id, created_at, updated_at)
VALUES ('aaaa1111-1111-1111-1111-111111111111', 'Risk in Demo Mine', 'Mine', 'A1', '一般', 'measures', '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO risks (risk_id, description, category, location, level, control_measures, responsible_org_id, responsible_user_id, created_at, updated_at)
VALUES ('bbbb2222-2222-2222-2222-222222222222', 'Risk in Other Org', 'Mine', 'B2', '一般', 'measures', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Hazards linked to risks (one in Demo Mine org, one in Other Org)
INSERT INTO hazards (hazard_id, description, status, level, location, reporter_id, reported_at, rectification_deadline, rectifier_id, verifier_id, risk_id, gov_flag, gov_source, created_at, updated_at)
VALUES ('cccc3333-3333-3333-3333-333333333333', 'Hazard A', '待指派', '一般', 'LocA', '33333333-3333-3333-3333-333333333333', CURRENT_TIMESTAMP, NULL, NULL, NULL, 'aaaa1111-1111-1111-1111-111111111111', FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO hazards (hazard_id, description, status, level, location, reporter_id, reported_at, rectification_deadline, rectifier_id, verifier_id, risk_id, gov_flag, gov_source, created_at, updated_at)
VALUES ('dddd4444-4444-4444-4444-444444444444', 'Hazard B', '待指派', '一般', 'LocB', '33333333-3333-3333-3333-333333333333', CURRENT_TIMESTAMP, NULL, NULL, NULL, 'bbbb2222-2222-2222-2222-222222222222', FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
