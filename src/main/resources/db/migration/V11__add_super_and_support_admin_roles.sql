-- V11__add_super_and_support_admin_roles.sql
-- Add SUPER_ADMIN and SUPPORT_ADMIN roles, migrate ADMIN users to SUPER_ADMIN, and remove old ADMIN role.

INSERT INTO roles (name) VALUES ('SUPER_ADMIN'), ('SUPPORT_ADMIN');

UPDATE users SET role = 'SUPER_ADMIN' WHERE role = 'ADMIN';

DELETE FROM roles WHERE name = 'ADMIN';
