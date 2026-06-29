-- =============================================================================
-- SecDash - Dados de demonstração
-- =============================================================================
-- Popula a base de dados com dados realistas para a apresentação.
--
-- Credenciais (login por nome de utilizador):
--   * Utilizador normal : teste  / teste   -> pertence a uma equipa com repos,
--                                              vulnerabilidades e alertas SAST.
--   * Administrador     : admin  / admin   -> sem equipas/repos (vê tudo na app).
--   * Colaborador       : colaborador / teste -> 2º membro da equipa (opcional,
--                                              enriquece a página da equipa).
--
-- O script usa um bloco DO com RETURNING ... INTO para capturar os IDs gerados,
-- por isso pode ser executado numa base de dados que já tenha outros dados
-- (não depende de IDs fixos). As tabelas têm de existir (ver schema.sql).
--
-- Para inserir:  psql "$DATABASE_URL" -f demo-data.sql
-- =============================================================================

DO $$
DECLARE
    v_teste_uid INT;
    v_admin_uid INT;
    v_colab_uid INT;

    v_gh_owner  INT;
    v_gl_owner  INT;

    v_repo_web  INT;  -- web-app        (GITHUB) - repo principal, muitos dados
    v_repo_api  INT;  -- payment-api    (GITHUB)
    v_repo_mob  INT;  -- mobile-client  (GITLAB)
    v_repo_inf  INT;  -- infra-tools    (GITLAB) - poucos problemas

    v_team      INT;
BEGIN
    -- -------------------------------------------------------------------------
    -- Utilizadores
    -- -------------------------------------------------------------------------
    INSERT INTO users (name, password_validation, email, role)
    VALUES ('teste', '$2a$10$67q6s4mq4uwzlGTCdM7HOephDwMpPsunqTaN4U73iw7ZcDx3ekwaO', 'teste@secdash.demo', 'USER')  -- password: teste
    RETURNING uid INTO v_teste_uid;

    INSERT INTO users (name, password_validation, email, role)
    VALUES ('admin', '$2a$10$nDlz7gTqplnx.8vQvpTvNuwfCDtpcymSReqKEagkgKRP4JwB/6.7m', 'admin@secdash.demo', 'ADMIN')  -- password: admin
    RETURNING uid INTO v_admin_uid;

    INSERT INTO users (name, password_validation, email, role)
    VALUES ('colaborador', '$2a$10$67q6s4mq4uwzlGTCdM7HOephDwMpPsunqTaN4U73iw7ZcDx3ekwaO', 'colaborador@secdash.demo', 'USER')  -- password: teste
    RETURNING uid INTO v_colab_uid;

    -- -------------------------------------------------------------------------
    -- Owners (organizações nas plataformas)
    -- -------------------------------------------------------------------------
    INSERT INTO owners (external_id, name, url, avatar_url, platform)
    VALUES ('1001', 'secdash-demo', 'https://github.com/secdash-demo', 'https://avatars.githubusercontent.com/u/9919', 'GITHUB')
    RETURNING oid INTO v_gh_owner;

    INSERT INTO owners (external_id, name, url, avatar_url, platform)
    VALUES ('2001', 'secdash-demo', 'https://gitlab.com/secdash-demo', 'https://gitlab.com/uploads/-/system/group/avatar/2001/logo.png', 'GITLAB')
    RETURNING oid INTO v_gl_owner;

    -- -------------------------------------------------------------------------
    -- Repositórios
    -- -------------------------------------------------------------------------
    INSERT INTO repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES ('web-app', '5001', 'GITHUB', v_gh_owner, 'https://github.com/secdash-demo/web-app', 'Aplicação web voltada para o cliente (React + Node).', 18, '2025-09-12 10:00:00+00', '2026-06-27 16:30:00+00', 7, 'PUBLIC')
    RETURNING rid INTO v_repo_web;

    INSERT INTO repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES ('payment-api', '5002', 'GITHUB', v_gh_owner, 'https://github.com/secdash-demo/payment-api', 'Serviço de pagamentos (Spring Boot).', 9, '2025-10-03 09:30:00+00', '2026-06-26 11:15:00+00', 3, 'PRIVATE')
    RETURNING rid INTO v_repo_api;

    INSERT INTO repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES ('mobile-client', '6001', 'GITLAB', v_gl_owner, 'https://gitlab.com/secdash-demo/mobile-client', 'Aplicação móvel (Kotlin/Android).', 5, '2025-11-20 14:00:00+00', '2026-06-25 18:45:00+00', 1, 'PRIVATE')
    RETURNING rid INTO v_repo_mob;

    INSERT INTO repositories (name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
    VALUES ('infra-tools', '6002', 'GITLAB', v_gl_owner, 'https://gitlab.com/secdash-demo/infra-tools', 'Scripts de infraestrutura e IaC (Terraform).', 2, '2026-01-15 08:00:00+00', '2026-06-22 10:05:00+00', 0, 'INTERNAL')
    RETURNING rid INTO v_repo_inf;

    -- -------------------------------------------------------------------------
    -- Acesso dos utilizadores aos repositórios
    -- -------------------------------------------------------------------------
    INSERT INTO user_repositories (uid, rid) VALUES
        (v_teste_uid, v_repo_web),
        (v_teste_uid, v_repo_api),
        (v_teste_uid, v_repo_mob),
        (v_teste_uid, v_repo_inf),
        (v_colab_uid, v_repo_web),
        (v_colab_uid, v_repo_api),
        (v_colab_uid, v_repo_mob),
        (v_colab_uid, v_repo_inf);

    -- -------------------------------------------------------------------------
    -- Equipa
    -- -------------------------------------------------------------------------
    INSERT INTO teams (name, description, last_scan_at)
    VALUES ('Equipa Demo SecDash', 'Equipa de demonstração com vários repositórios monitorizados.', '2026-06-27 09:00:00+00')
    RETURNING tid INTO v_team;

    INSERT INTO team_users (tid, uid, role) VALUES
        (v_team, v_teste_uid, 'LEADER'),
        (v_team, v_colab_uid, 'COLLABORATOR');

    INSERT INTO team_repos (tid, rid) VALUES
        (v_team, v_repo_web),
        (v_team, v_repo_api),
        (v_team, v_repo_mob),
        (v_team, v_repo_inf);

    -- =========================================================================
    -- VULNERABILIDADES (dependências)
    -- =========================================================================

    -- ---- web-app (GITHUB) ----
    INSERT INTO vulnerabilities (external_id, title, description, severity, state, cve_id, ghsa_id, package_name, package_version, vulnerable_version_range, fixed_version, manifest_path, cvss_score, cvss_vector, platform, rid, detected_at, updated_at) VALUES
        ('GHSA-WA-1', 'Prototype Pollution in lodash', 'Permite poluição do prototype através de funções de merge.', 'CRITICAL', 'OPEN', 'CVE-2021-23337', 'GHSA-35jh-r3h4-6jhm', 'lodash', '4.17.20', '< 4.17.21', '4.17.21', 'package.json', 7.2, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:H', 'GITHUB', v_repo_web, '2026-06-10 10:00:00', '2026-06-25 10:00:00'),
        ('GHSA-WA-2', 'Server-Side Request Forgery in axios', 'Pedidos podem ser redirecionados para hosts internos.', 'HIGH', 'OPEN', 'CVE-2023-45857', 'GHSA-wf5p-g6vw-rhxx', 'axios', '1.5.0', '< 1.6.0', '1.6.0', 'package.json', 6.5, 'CVSS:3.1/AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:N/A:N', 'GITHUB', v_repo_web, '2026-06-12 10:00:00', '2026-06-25 10:00:00'),
        ('GHSA-WA-3', 'ReDoS in semver', 'Expressão regular vulnerável a denial of service.', 'MEDIUM', 'OPEN', 'CVE-2022-25883', 'GHSA-c2qf-rxjj-qqgw', 'semver', '7.3.5', '< 7.5.2', '7.5.2', 'package.json', 5.3, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L', 'GITHUB', v_repo_web, '2026-06-12 10:00:00', '2026-06-25 10:00:00'),
        ('GHSA-WA-4', 'Inefficient Regular Expression in minimatch', 'Pode causar bloqueio do event loop.', 'LOW', 'FIXED', 'CVE-2022-3517', 'GHSA-f8q6-p94x-37v3', 'minimatch', '3.0.4', '< 3.0.5', '3.0.5', 'package.json', 3.7, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:N/I:N/A:L', 'GITHUB', v_repo_web, '2026-05-30 10:00:00', '2026-06-20 10:00:00'),
        ('GHSA-WA-5', 'Open redirect in follow-redirects', 'Redirecionamento para URL controlado pelo atacante.', 'HIGH', 'DISMISSED', 'CVE-2024-28849', 'GHSA-cxjh-pqwp-8mfp', 'follow-redirects', '1.15.4', '< 1.15.6', '1.15.6', 'package.json', 6.5, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:C/C:L/I:L/A:N', 'GITHUB', v_repo_web, '2026-06-05 10:00:00', '2026-06-18 10:00:00');

    -- ---- payment-api (GITHUB) ----
    INSERT INTO vulnerabilities (external_id, title, description, severity, state, cve_id, ghsa_id, package_name, package_version, vulnerable_version_range, fixed_version, manifest_path, cvss_score, cvss_vector, platform, rid, detected_at, updated_at) VALUES
        ('GHSA-API-1', 'Deserialization of Untrusted Data in jackson-databind', 'Permite execução remota de código via gadgets.', 'CRITICAL', 'OPEN', 'CVE-2020-36518', 'GHSA-57j2-w4cx-62h2', 'com.fasterxml.jackson.core:jackson-databind', '2.12.6', '< 2.12.7.1', '2.12.7.1', 'pom.xml', 7.5, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H', 'GITHUB', v_repo_api, '2026-06-08 10:00:00', '2026-06-24 10:00:00'),
        ('GHSA-API-2', 'SQL Injection in hibernate-core', 'Construção insegura de queries HQL.', 'HIGH', 'OPEN', 'CVE-2020-25638', 'GHSA-mw36-7c6c-q4q2', 'org.hibernate:hibernate-core', '5.4.18', '< 5.4.24', '5.4.24', 'pom.xml', 7.4, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:N/A:H', 'GITHUB', v_repo_api, '2026-06-09 10:00:00', '2026-06-24 10:00:00'),
        ('GHSA-API-3', 'Denial of Service in snakeyaml', 'Stack overflow ao processar YAML profundamente aninhado.', 'MEDIUM', 'OPEN', 'CVE-2022-25857', 'GHSA-mjmj-j48q-9wg2', 'org.yaml:snakeyaml', '1.30', '< 1.31', '1.31', 'pom.xml', 5.5, 'CVSS:3.1/AV:L/AC:L/PR:N/UI:R/S:U/C:N/I:N/A:H', 'GITHUB', v_repo_api, '2026-06-09 10:00:00', '2026-06-24 10:00:00');

    -- ---- mobile-client (GITLAB) ----
    INSERT INTO vulnerabilities (external_id, title, description, severity, state, cve_id, ghsa_id, package_name, package_version, vulnerable_version_range, fixed_version, manifest_path, cvss_score, cvss_vector, platform, rid, detected_at, updated_at) VALUES
        ('GL-MOB-1', 'Improper Certificate Validation in okhttp', 'Validação incorreta de certificados em determinadas configurações.', 'HIGH', 'OPEN', 'CVE-2021-0341', NULL, 'com.squareup.okhttp3:okhttp', '4.9.0', '< 4.9.2', '4.9.2', 'build.gradle', 7.4, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:N/A:N', 'GITLAB', v_repo_mob, '2026-06-11 10:00:00', '2026-06-23 10:00:00'),
        ('GL-MOB-2', 'Information Exposure in Gson', 'Exposição de informação sensível em mensagens de erro.', 'MEDIUM', 'OPEN', 'CVE-2022-25647', NULL, 'com.google.code.gson:gson', '2.8.6', '< 2.8.9', '2.8.9', 'build.gradle', 5.9, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:N/A:N', 'GITLAB', v_repo_mob, '2026-06-11 10:00:00', '2026-06-23 10:00:00'),
        ('GL-MOB-3', 'Uncontrolled Resource Consumption in kotlin-stdlib', 'Possível esgotamento de recursos.', 'LOW', 'OPEN', 'CVE-2022-24329', NULL, 'org.jetbrains.kotlin:kotlin-stdlib', '1.6.0', '< 1.6.10', '1.6.10', 'build.gradle', 3.3, 'CVSS:3.1/AV:L/AC:L/PR:L/UI:N/S:U/C:N/I:N/A:L', 'GITLAB', v_repo_mob, '2026-06-11 10:00:00', '2026-06-23 10:00:00');

    -- ---- infra-tools (GITLAB) - apenas 1 vulnerabilidade de baixa severidade ----
    INSERT INTO vulnerabilities (external_id, title, description, severity, state, cve_id, ghsa_id, package_name, package_version, vulnerable_version_range, fixed_version, manifest_path, cvss_score, cvss_vector, platform, rid, detected_at, updated_at) VALUES
        ('GL-INF-1', 'Sensitive information in logs (requests)', 'Cabeçalhos de autenticação podem ser registados.', 'LOW', 'OPEN', 'CVE-2023-32681', NULL, 'requests', '2.28.0', '< 2.31.0', '2.31.0', 'requirements.txt', 6.1, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:C/C:L/I:L/A:N', 'GITLAB', v_repo_inf, '2026-06-14 10:00:00', '2026-06-22 10:00:00');

    -- -------------------------------------------------------------------------
    -- Referências de vulnerabilidades (páginas de detalhe)
    -- -------------------------------------------------------------------------
    INSERT INTO vulnerability_references (vuln_id, url)
    SELECT vid, url FROM (
        VALUES
            ('GHSA-WA-1', 'GITHUB'::platform, v_repo_web, 'https://github.com/advisories/GHSA-35jh-r3h4-6jhm'),
            ('GHSA-WA-1', 'GITHUB'::platform, v_repo_web, 'https://nvd.nist.gov/vuln/detail/CVE-2021-23337'),
            ('GHSA-WA-2', 'GITHUB'::platform, v_repo_web, 'https://github.com/advisories/GHSA-wf5p-g6vw-rhxx'),
            ('GHSA-WA-3', 'GITHUB'::platform, v_repo_web, 'https://github.com/advisories/GHSA-c2qf-rxjj-qqgw'),
            ('GHSA-API-1', 'GITHUB'::platform, v_repo_api, 'https://github.com/advisories/GHSA-57j2-w4cx-62h2'),
            ('GHSA-API-2', 'GITHUB'::platform, v_repo_api, 'https://github.com/advisories/GHSA-mw36-7c6c-q4q2'),
            ('GL-MOB-1', 'GITLAB'::platform, v_repo_mob, 'https://nvd.nist.gov/vuln/detail/CVE-2021-0341'),
            ('GL-INF-1', 'GITLAB'::platform, v_repo_inf, 'https://nvd.nist.gov/vuln/detail/CVE-2023-32681')
    ) AS refs(ext_id, plat, rid, url)
    JOIN vulnerabilities v ON v.external_id = refs.ext_id AND v.platform = refs.plat AND v.rid = refs.rid;

    -- =========================================================================
    -- ALERTAS SAST (análise estática do código)
    -- =========================================================================

    -- ---- web-app (GITHUB) ----
    INSERT INTO sast_alerts (rid, external_id, state, severity, rule_id, rule_description, tool_name, file_path, start_line, end_line, message, html_url, platform, detected_at, updated_at) VALUES
        (v_repo_web, 'SAST-WA-1', 'OPEN', 'CRITICAL', 'js/sql-injection', 'Query de base de dados construída a partir de input do utilizador.', 'CodeQL', 'src/api/users.js', 88, 92, 'Esta query depende de um valor fornecido pelo utilizador.', 'https://github.com/secdash-demo/web-app/security/code-scanning/1', 'GITHUB', '2026-06-10 10:00:00', '2026-06-25 10:00:00'),
        (v_repo_web, 'SAST-WA-2', 'OPEN', 'HIGH', 'js/reflected-xss', 'Cross-site scripting refletido.', 'CodeQL', 'src/views/profile.jsx', 24, 24, 'Dados não confiáveis escritos na página.', 'https://github.com/secdash-demo/web-app/security/code-scanning/2', 'GITHUB', '2026-06-11 10:00:00', '2026-06-25 10:00:00'),
        (v_repo_web, 'SAST-WA-3', 'OPEN', 'MEDIUM', 'js/weak-cryptographic-algorithm', 'Uso de algoritmo criptográfico fraco (MD5).', 'CodeQL', 'src/utils/hash.js', 12, 12, 'MD5 não deve ser usado para fins de segurança.', 'https://github.com/secdash-demo/web-app/security/code-scanning/3', 'GITHUB', '2026-06-11 10:00:00', '2026-06-25 10:00:00'),
        (v_repo_web, 'SAST-WA-4', 'FIXED', 'LOW', 'js/hardcoded-credentials', 'Credenciais hardcoded no código.', 'CodeQL', 'src/config/dev.js', 5, 5, 'Senha presente diretamente no código fonte.', 'https://github.com/secdash-demo/web-app/security/code-scanning/4', 'GITHUB', '2026-05-28 10:00:00', '2026-06-15 10:00:00');

    -- ---- payment-api (GITHUB) ----
    INSERT INTO sast_alerts (rid, external_id, state, severity, rule_id, rule_description, tool_name, file_path, start_line, end_line, message, html_url, platform, detected_at, updated_at) VALUES
        (v_repo_api, 'SAST-API-1', 'OPEN', 'HIGH', 'java/path-injection', 'Caminho de ficheiro construído a partir de input do utilizador.', 'CodeQL', 'src/main/java/com/demo/FileController.java', 47, 49, 'Acesso a ficheiro depende de valor não confiável.', 'https://github.com/secdash-demo/payment-api/security/code-scanning/1', 'GITHUB', '2026-06-09 10:00:00', '2026-06-24 10:00:00'),
        (v_repo_api, 'SAST-API-2', 'OPEN', 'MEDIUM', 'java/insecure-randomness', 'Uso de gerador de números aleatórios não seguro.', 'CodeQL', 'src/main/java/com/demo/TokenService.java', 31, 31, 'java.util.Random não é adequado para segurança.', 'https://github.com/secdash-demo/payment-api/security/code-scanning/2', 'GITHUB', '2026-06-09 10:00:00', '2026-06-24 10:00:00');

    -- ---- mobile-client (GITLAB) ----
    INSERT INTO sast_alerts (rid, external_id, state, severity, rule_id, rule_description, tool_name, file_path, start_line, end_line, message, html_url, platform, detected_at, updated_at) VALUES
        (v_repo_mob, 'SAST-MOB-1', 'OPEN', 'HIGH', 'kotlin.lang.security.cleartext-traffic', 'Tráfego em texto não cifrado permitido.', 'Semgrep', 'app/src/main/AndroidManifest.xml', 14, 14, 'usesCleartextTraffic está ativado.', 'https://gitlab.com/secdash-demo/mobile-client/-/security/vulnerabilities/1', 'GITLAB', '2026-06-11 10:00:00', '2026-06-23 10:00:00'),
        (v_repo_mob, 'SAST-MOB-2', 'OPEN', 'MEDIUM', 'kotlin.lang.security.logcat-leak', 'Possível fuga de dados sensíveis via Log.', 'Semgrep', 'app/src/main/java/com/demo/LoginActivity.kt', 73, 73, 'Token registado em Logcat.', 'https://gitlab.com/secdash-demo/mobile-client/-/security/vulnerabilities/2', 'GITLAB', '2026-06-11 10:00:00', '2026-06-23 10:00:00');

    -- ---- infra-tools (GITLAB) ----
    INSERT INTO sast_alerts (rid, external_id, state, severity, rule_id, rule_description, tool_name, file_path, start_line, end_line, message, html_url, platform, detected_at, updated_at) VALUES
        (v_repo_inf, 'SAST-INF-1', 'OPEN', 'MEDIUM', 'terraform.aws.security.aws-s3-bucket-public', 'Bucket S3 potencialmente público.', 'Semgrep', 'modules/storage/main.tf', 22, 28, 'ACL do bucket permite acesso público.', 'https://gitlab.com/secdash-demo/infra-tools/-/security/vulnerabilities/1', 'GITLAB', '2026-06-14 10:00:00', '2026-06-22 10:00:00');

    -- =========================================================================
    -- HISTÓRICO DE SCANS (gráficos de evolução temporal)
    -- =========================================================================

    -- ---- Vulnerabilidades por dia ----
    INSERT INTO repo_vulnerability_scans (rid, scanned_at, vulnerability_count, critical_count, high_count, medium_count, low_count, unknown_count) VALUES
        -- web-app: tendência decrescente (5 -> 4 abertas) ao longo do tempo
        (v_repo_web, '2026-06-05 09:00:00+00', 5, 1, 2, 1, 1, 0),
        (v_repo_web, '2026-06-12 09:00:00+00', 5, 1, 2, 1, 1, 0),
        (v_repo_web, '2026-06-19 09:00:00+00', 4, 1, 2, 1, 0, 0),
        (v_repo_web, '2026-06-26 09:00:00+00', 3, 1, 1, 1, 0, 0),
        -- payment-api
        (v_repo_api, '2026-06-08 09:00:00+00', 3, 1, 1, 1, 0, 0),
        (v_repo_api, '2026-06-15 09:00:00+00', 3, 1, 1, 1, 0, 0),
        (v_repo_api, '2026-06-24 09:00:00+00', 3, 1, 1, 1, 0, 0),
        -- mobile-client
        (v_repo_mob, '2026-06-11 09:00:00+00', 3, 0, 1, 1, 1, 0),
        (v_repo_mob, '2026-06-18 09:00:00+00', 3, 0, 1, 1, 1, 0),
        (v_repo_mob, '2026-06-23 09:00:00+00', 3, 0, 1, 1, 1, 0),
        -- infra-tools
        (v_repo_inf, '2026-06-14 09:00:00+00', 1, 0, 0, 0, 1, 0),
        (v_repo_inf, '2026-06-22 09:00:00+00', 1, 0, 0, 0, 1, 0);

    -- ---- Alertas SAST por dia ----
    INSERT INTO repo_sast_scans (rid, scanned_at, alert_count, critical_count, high_count, medium_count, low_count, unknown_count) VALUES
        -- web-app: tendência decrescente (4 -> 3 abertas)
        (v_repo_web, '2026-06-05 09:00:00+00', 4, 1, 1, 1, 1, 0),
        (v_repo_web, '2026-06-12 09:00:00+00', 4, 1, 1, 1, 1, 0),
        (v_repo_web, '2026-06-19 09:00:00+00', 3, 1, 1, 1, 0, 0),
        (v_repo_web, '2026-06-26 09:00:00+00', 3, 1, 1, 1, 0, 0),
        -- payment-api
        (v_repo_api, '2026-06-09 09:00:00+00', 2, 0, 1, 1, 0, 0),
        (v_repo_api, '2026-06-16 09:00:00+00', 2, 0, 1, 1, 0, 0),
        (v_repo_api, '2026-06-24 09:00:00+00', 2, 0, 1, 1, 0, 0),
        -- mobile-client
        (v_repo_mob, '2026-06-11 09:00:00+00', 2, 0, 1, 1, 0, 0),
        (v_repo_mob, '2026-06-23 09:00:00+00', 2, 0, 1, 1, 0, 0),
        -- infra-tools
        (v_repo_inf, '2026-06-14 09:00:00+00', 1, 0, 0, 1, 0, 0),
        (v_repo_inf, '2026-06-22 09:00:00+00', 1, 0, 0, 1, 0, 0);

    RAISE NOTICE 'Dados de demonstração inseridos: utilizadores (teste/admin/colaborador), 1 equipa, 4 repositórios, vulnerabilidades, alertas SAST e histórico de scans.';
END $$;