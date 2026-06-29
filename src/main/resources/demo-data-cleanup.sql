-- =============================================================================
-- SecDash - Remoção dos dados de demonstração
-- =============================================================================
-- Remove APENAS os dados inseridos por demo-data.sql, deixando intactos
-- quaisquer outros dados reais que existam na base de dados.
--
-- Os dados de demo são identificados pelos seguintes marcadores:
--   * utilizadores : email termina em '@secdash.demo'
--   * owners       : name = 'secdash-demo'  (external_id '1001'/'2001')
--   * repositórios : pertencem a esses owners
--   * equipa       : name = 'Equipa Demo SecDash'
--
-- As eliminações são feitas pela ordem correta das foreign keys. É seguro
-- executar mesmo que os dados de demo já não existam (não apaga nada).
--
-- Para executar:  psql "$DATABASE_URL" -f demo-data-cleanup.sql
-- =============================================================================

DO $$
DECLARE
    demo_user_ids  INT[];
    demo_owner_ids INT[];
    demo_repo_ids  INT[];
    demo_team_ids  INT[];
BEGIN
    -- Identificar os IDs dos dados de demonstração ----------------------------
    SELECT array_agg(uid) INTO demo_user_ids
    FROM users WHERE email LIKE '%@secdash.demo';

    SELECT array_agg(oid) INTO demo_owner_ids
    FROM owners WHERE name = 'secdash-demo' AND external_id IN ('1001', '2001');

    SELECT array_agg(rid) INTO demo_repo_ids
    FROM repositories WHERE owner_id = ANY(demo_owner_ids);

    SELECT array_agg(tid) INTO demo_team_ids
    FROM teams WHERE name = 'Equipa Demo SecDash';

    -- Eliminar pela ordem das dependências (filhos primeiro) ------------------

    -- Dados de segurança dos repositórios
    DELETE FROM vulnerability_references
    WHERE vuln_id IN (SELECT vid FROM vulnerabilities WHERE rid = ANY(demo_repo_ids));

    DELETE FROM vulnerabilities       WHERE rid = ANY(demo_repo_ids);
    DELETE FROM sast_alerts           WHERE rid = ANY(demo_repo_ids);
    DELETE FROM repo_vulnerability_scans WHERE rid = ANY(demo_repo_ids);
    DELETE FROM repo_sast_scans       WHERE rid = ANY(demo_repo_ids);

    -- Ligações da equipa e dos utilizadores aos repositórios
    DELETE FROM team_repos
    WHERE rid = ANY(demo_repo_ids) OR tid = ANY(demo_team_ids);

    DELETE FROM user_repositories
    WHERE rid = ANY(demo_repo_ids) OR uid = ANY(demo_user_ids);

    -- Repositórios e owners
    DELETE FROM repositories WHERE rid = ANY(demo_repo_ids);
    DELETE FROM owners       WHERE oid = ANY(demo_owner_ids);

    -- Equipa e membros
    DELETE FROM team_users WHERE tid = ANY(demo_team_ids) OR uid = ANY(demo_user_ids);
    DELETE FROM teams      WHERE tid = ANY(demo_team_ids);

    -- Sessões / autenticação / autorização dos utilizadores de demo
    DELETE FROM tokens               WHERE user_id = ANY(demo_user_ids);
    DELETE FROM user_authorization   WHERE user_id = ANY(demo_user_ids);
    DELETE FROM user_authentication  WHERE user_id = ANY(demo_user_ids);

    -- Utilizadores de demo
    DELETE FROM users WHERE uid = ANY(demo_user_ids);

    RAISE NOTICE 'Dados de demonstração removidos: % utilizador(es), % owner(s), % repositório(s), % equipa(s).',
        coalesce(array_length(demo_user_ids, 1), 0),
        coalesce(array_length(demo_owner_ids, 1), 0),
        coalesce(array_length(demo_repo_ids, 1), 0),
        coalesce(array_length(demo_team_ids, 1), 0);
END $$;