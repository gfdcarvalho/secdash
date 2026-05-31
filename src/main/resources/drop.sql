-- Drop Tables
DROP TABLE IF EXISTS vulnerability_references;
DROP TABLE IF EXISTS repo_sast_scans;
DROP TABLE IF EXISTS sast_alerts;
DROP TABLE IF EXISTS repo_vulnerability_scans;
DROP TABLE IF EXISTS vulnerabilities;
DROP TABLE IF EXISTS user_repositories;
DROP TABLE IF EXISTS team_repos;
DROP TABLE IF EXISTS repositories;
DROP TABLE IF EXISTS owners;
DROP TABLE IF EXISTS user_authorization;
DROP TABLE IF EXISTS user_authentication;
DROP TABLE IF EXISTS tokens;
DROP TABLE IF EXISTS team_users;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS users;

-- Drop Enums
DROP TYPE IF EXISTS sast_state;
DROP TYPE IF EXISTS sast_severity;
DROP TYPE IF EXISTS vulnerability_state;
DROP TYPE IF EXISTS vulnerability_severity;
DROP TYPE IF EXISTS visibility;
DROP TYPE IF EXISTS platform;
DROP TYPE IF EXISTS auth_provider;
DROP TYPE IF EXISTS app_roles;
DROP TYPE IF EXISTS team_roles;