-- Seed base organization
INSERT INTO organizations (organization_id, name, type)
VALUES ('11111111-1111-1111-1111-111111111111', 'Demo Mine', 'MINE')
ON CONFLICT (organization_id) DO NOTHING;

-- Seed ADMIN role
INSERT INTO roles (role_id, role_name, permissions)
VALUES ('22222222-2222-2222-2222-222222222222', 'ADMIN', '[]'::jsonb)
ON CONFLICT (role_id) DO NOTHING;

-- Seed admin user with NOOP password encoder (admin123)
INSERT INTO users (user_id, username, password_hash, full_name, organization_id, is_enabled)
VALUES (
  '33333333-3333-3333-3333-333333333333',
  'admin',
  '{noop}admin123',
  'Administrator',
  '11111111-1111-1111-1111-111111111111',
  TRUE
)
ON CONFLICT (user_id) DO NOTHING;

-- Bind admin user to ADMIN role
INSERT INTO user_roles (user_id, role_id)
VALUES ('33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222')
ON CONFLICT DO NOTHING;

