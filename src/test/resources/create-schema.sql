-- init: drops and recreates all tables and enums
CREATE OR REPLACE FUNCTION init() RETURNS VOID AS $$
BEGIN
    DROP TABLE IF EXISTS sast_alerts CASCADE;
    DROP TABLE IF EXISTS user_repositories CASCADE;
    DROP TABLE IF EXISTS vulnerability_references CASCADE;
    DROP TABLE IF EXISTS vulnerabilities CASCADE;
    DROP TABLE IF EXISTS repositories CASCADE;
    DROP TABLE IF EXISTS owners CASCADE;
    DROP TABLE IF EXISTS tokens CASCADE;
    DROP TABLE IF EXISTS user_authorization CASCADE;
    DROP TABLE IF EXISTS user_authentication CASCADE;
    DROP TABLE IF EXISTS users CASCADE;

    DROP TYPE IF EXISTS platform CASCADE;
    DROP TYPE IF EXISTS visibility CASCADE;
    DROP TYPE IF EXISTS vulnerability_severity CASCADE;
    DROP TYPE IF EXISTS vulnerability_state CASCADE;
    DROP TYPE IF EXISTS sast_severity CASCADE;
    DROP TYPE IF EXISTS sast_state CASCADE;
    DROP TYPE IF EXISTS auth_provider CASCADE;

    CREATE TYPE platform AS ENUM ('GITHUB', 'GITLAB');
    CREATE TYPE visibility AS ENUM ('PUBLIC', 'PRIVATE', 'INTERNAL');
    CREATE TYPE vulnerability_severity AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN');
    CREATE TYPE vulnerability_state AS ENUM ('OPEN', 'FIXED', 'DISMISSED');
    CREATE TYPE sast_severity AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW');
    CREATE TYPE sast_state AS ENUM ('OPEN', 'FIXED', 'DISMISSED');
    CREATE TYPE auth_provider AS ENUM ('GOOGLE', 'GITHUB', 'GITLAB');

    CREATE TABLE users (
        uid                 INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        name                VARCHAR(255) NOT NULL,
        password_validation TEXT,
        email               VARCHAR(255) NOT NULL UNIQUE
    );

    CREATE TABLE user_authentication (
        user_id     INT           NOT NULL REFERENCES users(uid),
        provider    auth_provider NOT NULL,
        provider_id VARCHAR       NOT NULL,
        PRIMARY KEY (user_id, provider),
        UNIQUE      (provider, provider_id)
    );

    CREATE TABLE user_authorization (
        user_id      INT           NOT NULL REFERENCES users(uid),
        provider     auth_provider NOT NULL,
        access_token TEXT          NOT NULL,
        PRIMARY KEY  (user_id, provider)
    );

    CREATE TABLE tokens (
        token_validation VARCHAR(256) PRIMARY KEY,
        user_id          INT REFERENCES users(uid),
        created_at       BIGINT NOT NULL,
        last_used_at     BIGINT NOT NULL
    );

    CREATE TABLE owners (
        oid         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        external_id VARCHAR(255),
        name        VARCHAR(255) NOT NULL,
        url         VARCHAR(255) NOT NULL,
        avatar_url  VARCHAR(255),
        platform    platform     NOT NULL,
        UNIQUE (name, platform)
    );

    CREATE TABLE repositories (
        rid          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        name         VARCHAR(255) NOT NULL,
        external_id  VARCHAR(255) NOT NULL,
        platform     platform     NOT NULL,
        owner_id     INT          NOT NULL REFERENCES owners(oid),
        html_url     VARCHAR(255) NOT NULL,
        description  TEXT,
        issues_count INT          NOT NULL DEFAULT 0,
        created_at   TIMESTAMPTZ  NOT NULL,
        updated_at   TIMESTAMPTZ  NOT NULL,
        forks_count  INT          NOT NULL DEFAULT 0,
        visibility   visibility   NOT NULL,
        UNIQUE (external_id, platform)
    );

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
        updated_at               TIMESTAMP              NOT NULL
    );

    CREATE TABLE vulnerability_references (
        id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        vuln_id INT  NOT NULL REFERENCES vulnerabilities(vid),
        url     TEXT NOT NULL
    );

    CREATE TABLE user_repositories (
        uid INT NOT NULL REFERENCES users(uid),
        rid INT NOT NULL REFERENCES repositories(rid),
        PRIMARY KEY (uid, rid)
    );

    CREATE TABLE sast_alerts (
        sid      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        rid      INT           NOT NULL REFERENCES repositories(rid),
        state    sast_state    NOT NULL,
        severity sast_severity NOT NULL,
        scanner  VARCHAR(255)  NOT NULL,
        file     VARCHAR(255)  NOT NULL,
        line     VARCHAR(50)   NOT NULL
    );
END;
$$ LANGUAGE plpgsql;

-- test data for UserControllerTests
CREATE OR REPLACE FUNCTION test_data_for_UserControllerTests() RETURNS VOID AS $$
BEGIN
    INSERT INTO users (name, password_validation, email)
    VALUES  ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','testemail@test1.com'), -- testpassword1
            ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','testemail@test2.com'); -- testpassword2
END;
$$ LANGUAGE plpgsql;

-- test data for GithubControllerTests
CREATE OR REPLACE FUNCTION test_data_for_GithubControllerTests() RETURNS VOID AS $$
BEGIN
    INSERT INTO users (name, password_validation, email)
    VALUES  ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','testemail@test1.com'), -- testpassword1
            ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','testemail@test2.com'); -- testpassword2
    INSERT INTO user_authorization (user_id, provider, access_token)
    VALUES  (1,'GITHUB','testToken');
    INSERT INTO owners (external_id, name, url, avatar_url, platform)
    VALUES  ('123','testOwner','https://example.com/owner/repo','https://example.com/owner/avatar','GITHUB');
    INSERT INTO repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES  ('testRepository', '123','GITHUB',1,'https://www.example.com','test',0 , '2026-03-23 15:31:04.000000 +00:00','2026-03-23 16:30:55.000000 +00:00', 0, 'PUBLIC' );
    INSERT INTO user_repositories (uid, rid)
    VALUES  (1,1);
END;
$$ LANGUAGE plpgsql;

-- test data for GitlabControllerTests
CREATE OR REPLACE FUNCTION test_data_for_GitlabControllerTests() RETURNS VOID AS $$
BEGIN
    INSERT INTO users (name, password_validation, email)
    VALUES  ('testUsername1','$2a$10$pbZFnR8NSKtxZ5ERtXYqreiyZNTMFAb1efUBT0RnrKsYOn3PimMii','testemail@test1.com'), -- testpassword1
            ('testUsername2','$2a$10$iAWi2kF17dYVB.kBLzPIyugXkt6Wt5T0bpanI2HyryCyKY7qv4Vuq','testemail@test2.com'); -- testpassword2
    INSERT INTO user_authorization (user_id, provider, access_token)
    VALUES  (1,'GITLAB','testToken');
END;
$$ LANGUAGE plpgsql;