-- Enums
CREATE TYPE platform AS ENUM ('GITHUB', 'GITLABS');
CREATE TYPE visibility AS ENUM ('PUBLIC', 'PRIVATE', 'INTERNAL');
CREATE TYPE vulnerability_severity AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN');
CREATE TYPE vulnerability_state AS ENUM ('OPEN', 'FIXED', 'DISMISSED');
CREATE TYPE sast_severity AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW');
CREATE TYPE sast_state AS ENUM ('OPEN', 'FIXED', 'DISMISSED');

-- Users
CREATE TABLE users (
    uid                     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    password_validation     TEXT,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    google_id               VARCHAR(255) UNIQUE,
    github_id               VARCHAR(255) UNIQUE,
    github_access_token     VARCHAR(255) UNIQUE
);

-- tokens
CREATE TABLE tokens (
    token_validation VARCHAR(256) primary key,
    user_id int references users(uid),
    created_at bigint not null,
    last_used_at bigint not null
);

-- Owners
CREATE TABLE owners (
    oid        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    url        VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    platform   platform     NOT NULL
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
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    forks_count  INT          NOT NULL DEFAULT 0,
    visibility   visibility   NOT NULL
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
    rid            INT                    NOT NULL REFERENCES repositories(rid),
    detected_at              TIMESTAMP              NOT NULL,
    updated_at               TIMESTAMP              NOT NULL
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
    sid        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rid        INT           NOT NULL REFERENCES repositories(rid),
    state      sast_state    NOT NULL,
    severity   sast_severity NOT NULL,
    scanner    VARCHAR(255)  NOT NULL,
    file       VARCHAR(255)  NOT NULL,
    line       VARCHAR(50)   NOT NULL
);


