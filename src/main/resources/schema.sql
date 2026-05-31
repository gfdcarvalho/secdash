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
    user_id      INT           NOT NULL REFERENCES users(uid),
    provider     auth_provider NOT NULL,
    access_token TEXT          NOT NULL,
    PRIMARY KEY  (user_id, provider)
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
    vulnerability_count INT         NOT NULL
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
    scan_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rid         INT         NOT NULL REFERENCES repositories(rid),
    scanned_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    alert_count INT         NOT NULL
);

CREATE TABLE teams (
    tid         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
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