-- V4__add_roles_lookup_table.sql
-- Create a roles lookup table to enforce database-level integrity for user roles.

CREATE TABLE roles (
    name VARCHAR(32) PRIMARY KEY
);

INSERT INTO roles (name) VALUES 
('ADMIN'),
('MERCHANT'),
('RIDER'),
('CUSTOMER');

ALTER TABLE users 
ADD CONSTRAINT fk_users_role 
FOREIGN KEY (role) 
REFERENCES roles (name);
