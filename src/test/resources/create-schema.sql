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
    INSERT INTO owners (external_id, name, url, avatar_url, platform)
    VALUES  ('123','testOwner','https://example.com/owner/repo','https://example.com/owner/avatar','GITLAB');
    INSERT INTO repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES  ('testRepository', '123','GITLAB',1,'https://www.example.com','test',0 , '2026-03-23 15:31:04.000000 +00:00','2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC' ),
            ('testRepository1', '1234','GITLAB',1,'https://www.example.com','test',0 , '2026-03-23 15:31:04.000000 +00:00','2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC' );
    INSERT INTO user_repositories (uid, rid)
    VALUES  (1,1),
            (2,2);
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
create or replace function test_data_for_AdminControllerTests() returns void as $$
BEGIN
    INSERT INTO users (name, password_validation, email, role)
    VALUES
        -- password: testpassword1
        ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','test1@email.com', 'ADMIN'),
        -- password: testpassword2
        ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','test2@email.com', 'USER'),
        -- password: testpassword3
        ('testUsername3','$2a$10$.gAsQtGdm7JdjR/4kD9p1eT1L28cvCtAByxtqt0rpStbkq.9dqyqW','test3@email.com', 'USER'),
        -- password: testpassword4
        ('testUsername4','$2a$10$hXld1iw19GwU4O5NPk4GqO5a233ycPfP5Y/mMRP9g8P.blZ3L9H.u','test4@email.com', 'USER'),
        -- password: testpassword5
        ('testUsername5','$2a$10$3GKVyzFZdsXvdtx39y1U5eXEdSwAHNadnQXpIGnzmtWaiisrz5C7e','test5@email.com', 'USER');

    INSERT INTO teams (name, description, last_scan_at)
    VALUES
        ('testeTeam1', null, null);

    insert into team_users (tid, uid, role)
    VALUES
        (1, 2, 'LEADER');

END;
$$ LANGUAGE plpgsql;
create or replace function test_data_for_RepoControllerTests() returns void as $$
BEGIN
    INSERT INTO users (name, password_validation, email, role)
    VALUES
        -- password: testpassword1
        ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','test1@email.com', 'ADMIN'),
        -- password: testpassword2
        ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','test2@email.com', 'USER'),
        -- password: testpassword3
        ('testUsername3','$2a$10$.gAsQtGdm7JdjR/4kD9p1eT1L28cvCtAByxtqt0rpStbkq.9dqyqW','test3@email.com', 'USER'),
        -- password: testpassword4
        ('testUsername4','$2a$10$hXld1iw19GwU4O5NPk4GqO5a233ycPfP5Y/mMRP9g8P.blZ3L9H.u','test4@email.com', 'USER'),
        -- password: testpassword5
        ('testUsername5','$2a$10$3GKVyzFZdsXvdtx39y1U5eXEdSwAHNadnQXpIGnzmtWaiisrz5C7e','test5@email.com', 'USER');

    insert into owners (external_id, name, url, avatar_url, platform)
    VALUES
        ('123', 'testOwner', 'https://github.com/tests', 'https://github.com/tests/avatar', 'GITHUB'),
        ('456', 'testGitlabOwner', 'https://gitlab.com/tests', 'https://gitlab.com/tests/avatar', 'GITLAB');

    insert into repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES
        ('testRepo1', '12345', 'GITHUB', 1, 'https://github.com/tests/testRepo1', 'first test repo', 0, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC'),
        ('testRepo2', '12346', 'GITHUB', 1, 'https://github.com/tests/testRepo2', 'second test repo', 3, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 1, 'PRIVATE'),
        ('testRepo3', '12347', 'GITHUB', 1, 'https://github.com/tests/testRepo3', null, 0, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 0, 'INTERNAL'),
        ('testGitlabRepo1', '54321', 'GITLAB', 2, 'https://gitlab.com/tests/testGitlabRepo1', 'first gitlab test repo', 0, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC'),
        ('testGitlabRepo2', '54322', 'GITLAB', 2, 'https://gitlab.com/tests/testGitlabRepo2', 'second gitlab test repo', 2, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 1, 'PRIVATE');

    insert into user_repositories (uid, rid)
    VALUES
        (1, 1),
        (1, 2),
        (1, 3),
        (1, 4),
        (1, 5);

    insert into vulnerabilities (external_id, title, description, severity, state, cve_id, ghsa_id, package_name, package_version, vulnerable_version_range, fixed_version, manifest_path, cvss_score, cvss_vector, platform, rid, detected_at, updated_at)
    VALUES
        ('VULN-1', 'Prototype Pollution in lodash', 'Prototype pollution vulnerability', 'CRITICAL', 'OPEN', 'CVE-2021-23337', 'GHSA-35jh-r3h4-6jhm', 'lodash', '4.17.20', '< 4.17.21', '4.17.21', 'package.json', 7.2, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:H', 'GITHUB', 1, '2026-03-20 10:00:00', '2026-03-20 10:00:00'),
        ('VULN-2', 'ReDoS in ansi-regex', 'Regular expression denial of service', 'HIGH', 'OPEN', 'CVE-2021-3807', 'GHSA-93q8-gq69-wqmw', 'ansi-regex', '5.0.0', '< 5.0.1', '5.0.1', 'package.json', 5.3, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L', 'GITHUB', 1, '2026-03-21 10:00:00', '2026-03-21 10:00:00'),
        ('VULN-3', 'XSS in serialize-javascript', 'Cross-site scripting', 'MEDIUM', 'FIXED', 'CVE-2020-7660', 'GHSA-hxcc-f52p-wc94', 'serialize-javascript', '3.0.0', '< 3.1.0', '3.1.0', 'package.json', 4.2, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:L/I:L/A:N', 'GITHUB', 1, '2026-03-19 10:00:00', '2026-03-22 10:00:00'),
        ('VULN-4', 'Information exposure in tmp', 'Arbitrary file/directory write via symlink', 'LOW', 'DISMISSED', null, 'GHSA-52f5-9888-hmc6', 'tmp', '0.2.0', '< 0.2.1', '0.2.1', 'package.json', 2.5, null, 'GITHUB', 1, '2026-03-18 10:00:00', '2026-03-18 10:00:00');

    insert into vulnerability_references (vuln_id, url)
    VALUES
        (1, 'https://github.com/advisories/GHSA-35jh-r3h4-6jhm'),
        (1, 'https://nvd.nist.gov/vuln/detail/CVE-2021-23337'),
        (2, 'https://github.com/advisories/GHSA-93q8-gq69-wqmw');

    insert into sast_alerts (rid, external_id, state, severity, rule_id, rule_description, tool_name, file_path, start_line, end_line, message, html_url, platform, detected_at, updated_at)
    VALUES
        (1, 'SAST-1', 'OPEN', 'CRITICAL', 'js/sql-injection', 'Database query built from user-controlled sources', 'CodeQL', 'src/db.js', 42, 45, 'This query depends on a user-provided value', 'https://github.com/tests/testRepo1/security/code-scanning/1', 'GITHUB', '2026-03-20 10:00:00', '2026-03-20 10:00:00'),
        (1, 'SAST-2', 'OPEN', 'HIGH', 'js/reflected-xss', 'Reflected cross-site scripting', 'CodeQL', 'src/render.js', 12, 12, 'Untrusted data is written to the page', 'https://github.com/tests/testRepo1/security/code-scanning/2', 'GITHUB', '2026-03-21 10:00:00', '2026-03-21 10:00:00'),
        (1, 'SAST-3', 'FIXED', 'MEDIUM', 'js/weak-cryptographic-algorithm', 'Use of a weak hashing algorithm', 'CodeQL', 'src/auth.js', 88, 90, 'MD5 is cryptographically weak', 'https://github.com/tests/testRepo1/security/code-scanning/3', 'GITHUB', '2026-03-19 10:00:00', '2026-03-22 10:00:00');

    insert into repo_vulnerability_scans (rid, scanned_at, vulnerability_count, critical_count, high_count, medium_count, low_count, unknown_count)
    VALUES
        (1, '2026-03-18 09:00:00+00', 2, 0, 1, 1, 0, 0),
        (1, '2026-03-19 09:00:00+00', 3, 1, 1, 1, 0, 0),
        (1, '2026-03-20 09:00:00+00', 4, 1, 1, 1, 1, 0),
        (1, '2026-03-21 09:00:00+00', 4, 1, 2, 1, 0, 0);

    insert into repo_sast_scans (rid, scanned_at, alert_count, critical_count, high_count, medium_count, low_count, unknown_count)
    VALUES
        (1, '2026-03-18 09:00:00+00', 1, 0, 1, 0, 0, 0),
        (1, '2026-03-19 09:00:00+00', 2, 1, 1, 0, 0, 0),
        (1, '2026-03-20 09:00:00+00', 3, 1, 1, 1, 0, 0),
        (1, '2026-03-21 09:00:00+00', 3, 1, 2, 0, 0, 0);
END;
$$ LANGUAGE plpgsql;
create or replace function test_data_for_SastControllerTests() returns void as $$
BEGIN
    INSERT INTO users (name, password_validation, email, role)
    VALUES
        -- password: testpassword1
        ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','test1@email.com', 'ADMIN'),
        -- password: testpassword2
        ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','test2@email.com', 'USER'),
        -- password: testpassword3
        ('testUsername3','$2a$10$.gAsQtGdm7JdjR/4kD9p1eT1L28cvCtAByxtqt0rpStbkq.9dqyqW','test3@email.com', 'USER'),
        -- password: testpassword4
        ('testUsername4','$2a$10$hXld1iw19GwU4O5NPk4GqO5a233ycPfP5Y/mMRP9g8P.blZ3L9H.u','test4@email.com', 'USER'),
        -- password: testpassword5
        ('testUsername5','$2a$10$3GKVyzFZdsXvdtx39y1U5eXEdSwAHNadnQXpIGnzmtWaiisrz5C7e','test5@email.com', 'USER');

    insert into owners (external_id, name, url, avatar_url, platform)
    VALUES
        ('123', 'testOwner', 'https://github.com/tests', 'https://github.com/tests/avatar', 'GITHUB');

    insert into repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES
        ('testRepo1', '12345', 'GITHUB', 1, 'https://github.com/tests/testRepo1', 'repo with a sast alert', 0, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC');

    insert into user_repositories (uid, rid)
    VALUES
        (1, 1);

    insert into sast_alerts (rid, external_id, state, severity, rule_id, rule_description, tool_name, file_path, start_line, end_line, message, html_url, platform, detected_at, updated_at)
    VALUES
        (1, 'SAST-1', 'OPEN', 'CRITICAL', 'js/sql-injection', 'Database query built from user-controlled sources', 'CodeQL', 'src/db.js', 42, 45, 'This query depends on a user-provided value', 'https://github.com/tests/testRepo1/security/code-scanning/1', 'GITHUB', '2026-03-20 10:00:00', '2026-03-20 10:00:00');
END;
$$ LANGUAGE plpgsql;
create or replace function test_data_for_TeamControllerTests() returns void as $$
BEGIN
    INSERT INTO users (name, password_validation, email, role)
    VALUES
        -- password: testpassword1
        ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','test1@email.com', 'ADMIN'),
        -- password: testpassword2
        ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','test2@email.com', 'USER'),
        -- password: testpassword3
        ('testUsername3','$2a$10$.gAsQtGdm7JdjR/4kD9p1eT1L28cvCtAByxtqt0rpStbkq.9dqyqW','test3@email.com', 'USER'),
        -- password: testpassword4
        ('testUsername4','$2a$10$hXld1iw19GwU4O5NPk4GqO5a233ycPfP5Y/mMRP9g8P.blZ3L9H.u','test4@email.com', 'USER'),
        -- password: testpassword5
        ('testUsername5','$2a$10$3GKVyzFZdsXvdtx39y1U5eXEdSwAHNadnQXpIGnzmtWaiisrz5C7e','test5@email.com', 'USER');

    insert into owners (external_id, name, url, avatar_url, platform)
    VALUES
        ('123', 'testOwner', 'https://github.com/tests', 'https://github.com/tests/avatar', 'GITHUB');

    insert into repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES
        ('testRepo1', '12345', 'GITHUB', 1, 'https://github.com/tests/testRepo1', 'team1 repo with security data', 0, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC'),
        ('testRepo2', '12346', 'GITHUB', 1, 'https://github.com/tests/testRepo2', 'team1 repo without security data', 0, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC'),
        ('testRepo3', '12347', 'GITHUB', 1, 'https://github.com/tests/testRepo3', 'team2 repo', 0, '2026-03-23 15:31:04.000000 +00:00', '2026-03-23 16:30:55.000000 +00:00', 0, 'PRIVATE');

    insert into teams (name, description, last_scan_at)
    VALUES
        ('testTeam1', 'first test team', null),
        ('testTeam2', 'second test team', null);

    insert into team_users (tid, uid, role)
    VALUES
        (1, 1, 'LEADER'),        -- testUsername1 leads testTeam1
        (1, 2, 'COLLABORATOR'),  -- testUsername2 collaborates on testTeam1
        (2, 3, 'LEADER'),        -- testUsername3 leads testTeam2
        (2, 4, 'COLLABORATOR');  -- testUsername4 collaborates on testTeam2

    -- Team repositories: team1 owns repo1 (with data) and repo2 (empty); team2 owns repo3.
    insert into team_repos (tid, rid)
    VALUES
        (1, 1),
        (1, 2),
        (2, 3);

    -- ---- Security data for testRepo1 (rid 1) so testTeam1 has stats/history/vulns/sast ----
    insert into vulnerabilities (external_id, title, description, severity, state, cve_id, ghsa_id, package_name, package_version, vulnerable_version_range, fixed_version, manifest_path, cvss_score, cvss_vector, platform, rid, detected_at, updated_at)
    VALUES
        ('VULN-1', 'Prototype Pollution in lodash', 'Prototype pollution vulnerability', 'CRITICAL', 'OPEN', 'CVE-2021-23337', 'GHSA-35jh-r3h4-6jhm', 'lodash', '4.17.20', '< 4.17.21', '4.17.21', 'package.json', 7.2, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:H', 'GITHUB', 1, '2026-03-20 10:00:00', '2026-03-20 10:00:00'),
        ('VULN-2', 'ReDoS in ansi-regex', 'Regular expression denial of service', 'HIGH', 'OPEN', 'CVE-2021-3807', 'GHSA-93q8-gq69-wqmw', 'ansi-regex', '5.0.0', '< 5.0.1', '5.0.1', 'package.json', 5.3, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L', 'GITHUB', 1, '2026-03-21 10:00:00', '2026-03-21 10:00:00');

    insert into vulnerability_references (vuln_id, url)
    VALUES
        (1, 'https://github.com/advisories/GHSA-35jh-r3h4-6jhm'),
        (2, 'https://github.com/advisories/GHSA-93q8-gq69-wqmw');

    insert into sast_alerts (rid, external_id, state, severity, rule_id, rule_description, tool_name, file_path, start_line, end_line, message, html_url, platform, detected_at, updated_at)
    VALUES
        (1, 'SAST-1', 'OPEN', 'CRITICAL', 'js/sql-injection', 'Database query built from user-controlled sources', 'CodeQL', 'src/db.js', 42, 45, 'This query depends on a user-provided value', 'https://github.com/tests/testRepo1/security/code-scanning/1', 'GITHUB', '2026-03-20 10:00:00', '2026-03-20 10:00:00'),
        (1, 'SAST-2', 'OPEN', 'HIGH', 'js/reflected-xss', 'Reflected cross-site scripting', 'CodeQL', 'src/render.js', 12, 12, 'Untrusted data is written to the page', 'https://github.com/tests/testRepo1/security/code-scanning/2', 'GITHUB', '2026-03-21 10:00:00', '2026-03-21 10:00:00');

    insert into repo_vulnerability_scans (rid, scanned_at, vulnerability_count, critical_count, high_count, medium_count, low_count, unknown_count)
    VALUES
        (1, '2026-03-20 09:00:00+00', 1, 0, 1, 0, 0, 0),
        (1, '2026-03-21 09:00:00+00', 2, 1, 1, 0, 0, 0);

    insert into repo_sast_scans (rid, scanned_at, alert_count, critical_count, high_count, medium_count, low_count, unknown_count)
    VALUES
        (1, '2026-03-20 09:00:00+00', 1, 1, 0, 0, 0, 0),
        (1, '2026-03-21 09:00:00+00', 2, 1, 1, 0, 0, 0);
END;
$$ LANGUAGE plpgsql;


UPDATE users
SET role = 'ADMIN'
WHERE uid = 8