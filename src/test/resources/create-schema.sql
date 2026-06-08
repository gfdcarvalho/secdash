-- init: drops and recreates all tables and enums
CREATE OR REPLACE FUNCTION init() RETURNS VOID AS $$
BEGIN
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

-- Enums
    CREATE TYPE platform AS ENUM ('GITHUB', 'GITLAB');
    CREATE TYPE visibility AS ENUM ('PUBLIC', 'PRIVATE', 'INTERNAL');
    CREATE TYPE vulnerability_severity AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN');
    CREATE TYPE vulnerability_state AS ENUM ('OPEN', 'FIXED', 'DISMISSED');
    CREATE TYPE sast_severity AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN');
    CREATE TYPE sast_state AS ENUM ('OPEN', 'FIXED', 'DISMISSED');
    CREATE TYPE auth_provider AS ENUM ('GOOGLE', 'GITHUB', 'GITLAB');
    CREATE TYPE app_roles as ENUM ('ADMIN', 'USER');
    CREATE TYPE team_roles as ENUM ('LEADER', 'COLLABORATOR');
-- Users
    CREATE TABLE users (
                           uid                     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           name                    VARCHAR(255) NOT NULL,
                           password_validation     TEXT,
                           email                   VARCHAR(255) NOT NULL UNIQUE,
                           role                    app_roles NOT NULL DEFAULT 'USER'
    );

-- user_identities (authentication)
    CREATE TABLE user_authentication (
                                         user_id     INT           NOT NULL REFERENCES users(uid),
                                         provider    auth_provider NOT NULL,
                                         provider_id VARCHAR       NOT NULL,
                                         PRIMARY KEY (user_id, provider),
                                         UNIQUE      (provider, provider_id) -- dangerous!!
    );

-- user_oauth_tokens (authorization)
    CREATE TABLE user_authorization (
                                        user_id       INT           NOT NULL REFERENCES users(uid),
                                        provider      auth_provider NOT NULL,
                                        access_token  TEXT          NOT NULL,
                                        refresh_token TEXT,
                                        expires_at    TIMESTAMPTZ,
                                        PRIMARY KEY   (user_id, provider)
    );

-- tokens
    CREATE TABLE tokens (
                            token_validation    VARCHAR(256) primary key,
                            user_id             int references users(uid),
                            created_at          bigint not null,
                            last_used_at        bigint not null
    );

-- Owners
    CREATE TABLE owners (
                            oid             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            external_id     VARCHAR(255),
                            name            VARCHAR(255) NOT NULL,
                            url             VARCHAR(255) NOT NULL,
                            avatar_url      VARCHAR(255),
                            platform        platform     NOT NULL,
                            UNIQUE (external_id, platform)
    );

-- Repositories
    CREATE TABLE repositories (
                                  rid          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  name         VARCHAR(255) NOT NULL,
                                  external_id  VARCHAR(255) NOT NULL,
                                  platform     platform     NOT NULL,
                                  owner_id     INT          NOT NULL REFERENCES owners(oid),
                                  html_url     VARCHAR(255) NOT NULL,
                                  description  TEXT,
                                  issues_count INT          NOT NULL DEFAULT 0,
                                  created_at   TIMESTAMPTZ    NOT NULL,
                                  updated_at   TIMESTAMPTZ    NOT NULL,
                                  forks_count  INT          NOT NULL DEFAULT 0,
                                  visibility   visibility   NOT NULL,
                                  UNIQUE  (external_id, platform)
    );

-- Vulnerabilities
    CREATE TABLE vulnerabilities (
                                     vid                      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     external_id              VARCHAR(255)           NOT NULL,
                                     title                    VARCHAR(255)           NOT NULL,
                                     description              TEXT,
                                     severity                 vulnerability_severity NOT NULL,
                                     state                    vulnerability_state    NOT NULL,
                                     cve_id                   VARCHAR(50),
                                     ghsa_id                  VARCHAR(50),
                                     package_name             VARCHAR(255)           NOT NULL,
                                     package_version          VARCHAR(100),
                                     vulnerable_version_range VARCHAR(255),
                                     fixed_version            VARCHAR(100),
                                     manifest_path            VARCHAR(255),
                                     cvss_score               DOUBLE PRECISION,
                                     cvss_vector              VARCHAR(255),
                                     platform                 platform               NOT NULL,
                                     rid                      INT                    NOT NULL REFERENCES repositories(rid),
                                     detected_at              TIMESTAMP              NOT NULL,
                                     updated_at               TIMESTAMP              NOT NULL,
                                     UNIQUE (external_id, platform, rid)
    );

-- Vulnerability Scan History
    CREATE TABLE repo_vulnerability_scans (
                                              scan_id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              rid                 INT         NOT NULL REFERENCES repositories(rid),
                                              scanned_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                              vulnerability_count INT         NOT NULL,
                                              critical_count      INT         NOT NULL DEFAULT 0,
                                              high_count          INT         NOT NULL DEFAULT 0,
                                              medium_count        INT         NOT NULL DEFAULT 0,
                                              low_count           INT         NOT NULL DEFAULT 0,
                                              unknown_count       INT         NOT NULL DEFAULT 0
    );

-- Vulnerability References
    CREATE TABLE vulnerability_references (
                                              id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              vuln_id INT  NOT NULL REFERENCES vulnerabilities(vid),
                                              url     TEXT NOT NULL
    );

-- User Repositories
    CREATE TABLE user_repositories (
                                       uid INT NOT NULL REFERENCES users(uid),
                                       rid INT NOT NULL REFERENCES repositories(rid),
                                       PRIMARY KEY (uid, rid)
    );

-- SAST Alerts
    CREATE TABLE sast_alerts (
                                 sid              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 rid              INT           NOT NULL REFERENCES repositories(rid),
                                 external_id      VARCHAR(255)  NOT NULL,
                                 state            sast_state    NOT NULL,
                                 severity         sast_severity NOT NULL,
                                 rule_id          VARCHAR(255)  NOT NULL,
                                 rule_description TEXT          NOT NULL,
                                 tool_name        VARCHAR(255)  NOT NULL,
                                 file_path        VARCHAR(255),
                                 start_line       INT,
                                 end_line         INT,
                                 message          TEXT,
                                 html_url         VARCHAR(255)  NOT NULL,
                                 platform         platform      NOT NULL,
                                 detected_at      TIMESTAMPTZ,
                                 updated_at       TIMESTAMPTZ,
                                 UNIQUE (external_id, platform, rid)
    );

-- SAST Scan History
    CREATE TABLE repo_sast_scans (
                                     scan_id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     rid           INT         NOT NULL REFERENCES repositories(rid),
                                     scanned_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     alert_count   INT         NOT NULL,
                                     critical_count INT        NOT NULL DEFAULT 0,
                                     high_count     INT        NOT NULL DEFAULT 0,
                                     medium_count   INT        NOT NULL DEFAULT 0,
                                     low_count      INT        NOT NULL DEFAULT 0,
                                     unknown_count  INT        NOT NULL DEFAULT 0
    );

    CREATE TABLE teams (
                           tid          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           name         VARCHAR(255) NOT NULL,
                           description  TEXT,
                           last_scan_at TIMESTAMPTZ
    );

    CREATE TABLE team_users (
                                tid     INT NOT NULL REFERENCES teams(tid),
                                uid     INT NOT NULL REFERENCES users(uid),
                                role    team_roles NOT NULL,
                                PRIMARY KEY (tid, uid)
    );

    CREATE TABLE team_repos (
                                tid     INT NOT NULL REFERENCES teams(tid),
                                rid     INT NOT NULL REFERENCES repositories(rid),
                                PRIMARY KEY (tid, rid)
    );
END;
$$ LANGUAGE plpgsql;

-- test data for UserControllerTests
CREATE OR REPLACE FUNCTION test_data_for_UserControllerTests() RETURNS VOID AS $$
BEGIN
    INSERT INTO users (name, password_validation, email, role)
    VALUES  ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','testemail@test1.com', 'USER'), -- testpassword1
            ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','testemail@test2.com', 'USER'); -- testpassword2
END;
$$ LANGUAGE plpgsql;

-- test data for GithubControllerTests
CREATE OR REPLACE FUNCTION test_data_for_GithubControllerTests() RETURNS VOID AS $$
BEGIN
    INSERT INTO users (name, password_validation, email, role)
    VALUES  ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','testemail@test1.com', 'USER'), -- testpassword1
            ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','testemail@test2.com', 'USER'); -- testpassword2
    INSERT INTO user_authorization (user_id, provider, access_token)
    VALUES  (1,'GITHUB','testToken');
    INSERT INTO owners (external_id, name, url, avatar_url, platform)
    VALUES  ('123','testOwner','https://example.com/owner/repo','https://example.com/owner/avatar','GITHUB');
    INSERT INTO repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES  ('testRepository', '123','GITHUB',1,'https://www.example.com','test',0 , '2026-03-23 15:31:04.000000 +00:00','2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC' ),
            ('testRepository1', '1234','GITHUB',1,'https://www.example.com','test',0 , '2026-03-23 15:31:04.000000 +00:00','2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC' );
    INSERT INTO user_repositories (uid, rid)
    VALUES  (1,1),
            (2,2);
END;
$$ LANGUAGE plpgsql;

-- test data for GitlabControllerTests
CREATE OR REPLACE FUNCTION test_data_for_GitlabControllerTests() RETURNS VOID AS $$
BEGIN
    INSERT INTO users (name, password_validation, email, role)
    VALUES  ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','testemail@test1.com', 'USER'), -- testpassword1
            ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','testemail@test2.com', 'USER'); -- testpassword2
    INSERT INTO user_authorization (user_id, provider, access_token)
    VALUES  (1,'GITLAB','testToken');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION test_data_for_AuthControllerTests() RETURNS VOID AS $$
BEGIN
    INSERT INTO users (name, password_validation, email, role)
    VALUES
        -- password: testpassword1
        ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','test1@email.com', 'USER'),

        -- password: testpassword2
        ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','test2@email.com', 'USER');

    -- user1 already linked with GitHub login (external login scenario)
    INSERT INTO user_authentication (user_id, provider, provider_id)
    VALUES (1, 'GITHUB', 'gh-123');

    -- user1 already authorized GitHub API
    INSERT INTO user_authorization (user_id, provider, access_token)
    VALUES (1, 'GITHUB', 'testToken');
END;
$$ LANGUAGE plpgsql;