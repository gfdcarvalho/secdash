package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.repositories.RepositoryWithOwnerSqlDto
import com.isel.ps.secdash.model.sast.SastAlertWithRepoSqlDto
import com.isel.ps.secdash.model.sast.TeamSastAlerts
import com.isel.ps.secdash.model.teams.SastStats
import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.SimpleTeamWithCounts
import com.isel.ps.secdash.model.teams.StatRowSqlDto
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.model.teams.TeamMember
import com.isel.ps.secdash.model.teams.TeamRoles
import com.isel.ps.secdash.model.teams.TeamSastHistory
import com.isel.ps.secdash.model.teams.TeamScanTarget
import com.isel.ps.secdash.model.teams.TeamVulnerabilityHistory
import com.isel.ps.secdash.model.vulnerability.TeamVulnerabilities
import com.isel.ps.secdash.model.vulnerability.VulnerabilityWithRepoSqlDto
import com.isel.ps.secdash.model.teams.VulnerabilityStats
import com.isel.ps.secdash.model.vulnerability.VulnerabilityReferenceRow
import com.isel.ps.secdash.repository.interfaces.TeamRepositoryInterface
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class TeamRepository(
    private val handle: Handle,
) : TeamRepositoryInterface {

    override fun getTeamsByUser(uid: Int): List<SimpleTeamWithCounts> {
        return handle.createQuery(
            """
        SELECT
            t.tid,
            t.name,
            t.description,
            COUNT(DISTINCT tr.rid) AS repoCount,
            COUNT(DISTINCT tu_all.uid) AS memberCount
        FROM team_users tu
        JOIN teams t ON t.tid = tu.tid
        LEFT JOIN team_repos tr ON tr.tid = t.tid
        LEFT JOIN team_users tu_all ON tu_all.tid = t.tid
        WHERE tu.uid = :uid
        GROUP BY t.tid, t.name, t.description
        """
        )
            .bind("uid", uid)
            .mapTo<SimpleTeamWithCounts>()
            .list()
    }

    override fun getTeam(teamId: Int): Team? {
        val team = handle.createQuery("SELECT tid, name, description FROM teams WHERE tid = :tid")
            .bind("tid", teamId)
            .mapTo<SimpleTeam>()
            .findOne()
            .orElse(null) ?: return null

        val repos = handle.createQuery(
            """
            SELECT
                r.rid, r.name, r.external_id, r.platform, r.owner_id,
                r.html_url, r.description, r.issues_count,
                r.created_at, r.updated_at, r.forks_count, r.visibility,
                o.oid, o.external_id AS o_external_id, o.name AS o_name,
                o.url, o.avatar_url, o.platform AS o_platform
            FROM team_repos tr
            JOIN repositories r ON tr.rid = r.rid
            JOIN owners o ON r.owner_id = o.oid
            WHERE tr.tid = :tid
            """
        )
            .bind("tid", teamId)
            .mapTo<RepositoryWithOwnerSqlDto>()
            .list()
            .map { it.toDomain() }

        val members = handle.createQuery(
            """
            SELECT u.uid, u.name, u.email, tu.role AS team_role
            FROM team_users tu
            JOIN users u ON tu.uid = u.uid
            WHERE tu.tid = :tid
            """
        )
            .bind("tid", teamId)
            .mapTo<TeamMember>()
            .list()

        return Team(tid = team.tid, name = team.name, description = team.description, repos = repos, members = members)
    }

    override fun checkUserHasTeamAccess(tid: Int, uid: Int): Boolean {
        return handle.createQuery(
            """
                SELECT COUNT(*) FROM team_users
                WHERE tid = :tid AND uid = :uid
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("uid", uid)
            .mapTo<Int>()
            .one() > 0
    }

    override fun createTeam(uid: Int, teamName: String, description: String?): Int {
        val tid = handle.createUpdate(
            """
                INSERT INTO teams(name, description) VALUES (:teamName, :description)
            """.trimIndent()
        )
            .bind("teamName", teamName)
            .bind("description", description)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

        handle.createUpdate(
            """
                INSERT INTO team_users(tid, uid, role) VALUES (:tid, :uid, :role::team_roles)
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("uid", uid)
            .bind("role", TeamRoles.LEADER.name)
            .execute()
        return tid
    }

    override fun checkTeamExistence(tid: Int): Boolean { // entre esta maneira com o exists e o que esta acima com o count não sei qual é o melhor
        return handle.createQuery(
            """
                SELECT EXISTS(SELECT 1 FROM teams WHERE tid = :tid)
            """.trimIndent()
        )
            .bind("tid", tid)
            .mapTo<Boolean>()
            .one()
    }

    override fun checkUserTeamLeader(uid: Int, tid: Int): Boolean {
        return handle.createQuery(
            """
                SELECT EXISTS(SELECT 1 FROM team_users WHERE tid = :tid AND uid = :uid AND role = :role::team_roles)
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("uid", uid)
            .bind("role", TeamRoles.LEADER.name)
            .mapTo<Boolean>()
            .one()
    }

    override fun deleteTeam(tid: Int) {
        handle.createUpdate("DELETE FROM team_repos WHERE tid = :tid")
            .bind("tid", tid)
            .execute()

        handle.createUpdate("DELETE FROM team_users WHERE tid = :tid")
            .bind("tid", tid)
            .execute()

        handle.createUpdate("DELETE FROM teams WHERE tid = :tid")
            .bind("tid", tid)
            .execute()
    }

    override fun checkUserAlreadyOnTeam(tid: Int, uid: Int): Boolean {
        return handle.createQuery(
            """
                select exists(select 1 from team_users where tid = :tid and uid = :uid)
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("uid", uid)
            .mapTo<Boolean>()
            .one()
    }

    override fun addUserToTeam(tid: Int, userToAdd: Int) {
        handle.createUpdate(
            """
                INSERT INTO team_users (tid, uid, role) VALUES (:tid, :uid, :role::team_roles)
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("uid", userToAdd)
            .bind("role", TeamRoles.COLLABORATOR.name)
            .execute()
    }

    override fun removeUserFromTeam(tid: Int, userToRemove: Int) {
        handle.createUpdate(
            """
                DELETE FROM team_users WHERE tid = :tid AND uid = :uid
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("uid", userToRemove)
            .execute()
    }

    override fun promoteUserToLeader(tid: Int, userToPromote: Int) {
        handle.createUpdate(
            """
                update team_users set role = :role::team_roles where tid = :tid and uid = :uid
            """.trimIndent()
        )
            .bind("role", TeamRoles.LEADER.name)
            .bind("tid", tid)
            .bind("uid", userToPromote)
            .execute()
    }

    override fun checkTeamHasRepo(tid: Int, rid: Int): Boolean {
        return handle.createQuery(
            """
                select exists(select 1 from team_repos where tid = :tid and rid = :rid)
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("rid", rid)
            .mapTo<Boolean>()
            .one()
    }

    override fun addRepositoryToTeam(tid: Int, repositoryToAdd: Int) {
        handle.createUpdate(
            """
                insert into team_repos(tid, rid) values (:tid, :rid)
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("rid", repositoryToAdd)
            .execute()
    }

    override fun removeRepositoryFromTeam(tid: Int, repositoryToRemove: Int) {
        handle.createUpdate(
            """
                delete from team_repos where tid = :tid and rid = :rid
            """.trimIndent()
        )
            .bind("tid", tid)
            .bind("rid", repositoryToRemove)
            .execute()
    }

    override fun getTeamVulnerabilityStats(tid: Int): VulnerabilityStats {
        val rows = handle.createQuery(
            """
            SELECT state, severity, COUNT(*) AS count
            FROM vulnerabilities v
            JOIN team_repos tr ON v.rid = tr.rid
            WHERE tr.tid = :tid
            GROUP BY state, severity
            """.trimIndent()
        )
            .bind("tid", tid)
            .mapTo<StatRowSqlDto>()
            .list()
        return VulnerabilityStats.from(rows)
    }

    override fun getTeamSastStats(tid: Int): SastStats {
        val rows = handle.createQuery(
            """
            SELECT state, severity, COUNT(*) AS count
            FROM sast_alerts s
            JOIN team_repos tr ON s.rid = tr.rid
            WHERE tr.tid = :tid
            GROUP BY state, severity
            """.trimIndent()
        )
            .bind("tid", tid)
            .mapTo<StatRowSqlDto>()
            .list()
        return SastStats.from(rows)
    }

    override fun getTeamVulnerabilityHistory(tid: Int): List<TeamVulnerabilityHistory> {
        return handle.createQuery(
            """
            SELECT s.scan_id, s.rid, s.scanned_at, s.vulnerability_count,
                   s.critical_count, s.high_count, s.medium_count, s.low_count, s.unknown_count
            FROM repo_vulnerability_scans s
            JOIN team_repos tr ON s.rid = tr.rid
            WHERE tr.tid = :tid
            ORDER BY s.scanned_at
            """.trimIndent()
        )
            .bind("tid", tid)
            .mapTo<TeamVulnerabilityHistory>()
            .list()
    }

    override fun getTeamSastHistory(tid: Int): List<TeamSastHistory> {
        return handle.createQuery(
            """
            SELECT s.scan_id, s.rid, s.scanned_at, s.alert_count,
                   s.critical_count, s.high_count, s.medium_count, s.low_count, s.unknown_count
            FROM repo_sast_scans s
            JOIN team_repos tr ON s.rid = tr.rid
            WHERE tr.tid = :tid
            ORDER BY s.scanned_at
            """.trimIndent()
        )
            .bind("tid", tid)
            .mapTo<TeamSastHistory>()
            .list()
    }

    override fun getTeamsForScan(limit: Int): List<TeamScanTarget> {
        return handle.createQuery(
            """
            SELECT DISTINCT ON (t.tid) t.tid, tu.uid AS leader_uid
            FROM teams t
            JOIN team_users tu ON t.tid = tu.tid AND tu.role = :role::team_roles
            JOIN team_repos tr ON t.tid = tr.tid
            WHERE (t.last_scan_at IS NULL OR t.last_scan_at < NOW() - INTERVAL '24 hours')
              AND EXISTS (
                SELECT 1 FROM tokens tok
                WHERE tok.user_id = tu.uid
                  AND tok.last_used_at > EXTRACT(EPOCH FROM (NOW() - INTERVAL '48 hours'))
              )
            ORDER BY t.tid
            LIMIT :limit
            """.trimIndent()
        )
            .bind("limit", limit)
            .bind("role", TeamRoles.LEADER.name)
            .mapTo<TeamScanTarget>()
            .list()
    }

    override fun updateLastScanAt(tid: Int) {
        handle.createUpdate("UPDATE teams SET last_scan_at = NOW() WHERE tid = :tid")
            .bind("tid", tid)
            .execute()
    }

    override fun getTeamVulnerabilities(tid: Int): List<TeamVulnerabilities> {
        val rows = handle.createQuery(
            """
            SELECT v.vid, v.rid, r.name AS repo_name, v.external_id, v.title, v.description,
                   v.severity, v.state, v.cve_id, v.ghsa_id, v.package_name, v.package_version,
                   v.vulnerable_version_range, v.fixed_version, v.manifest_path,
                   v.cvss_score, v.cvss_vector, v.platform, v.detected_at, v.updated_at
            FROM vulnerabilities v
            JOIN repositories r ON v.rid = r.rid
            JOIN team_repos tr ON v.rid = tr.rid
            WHERE tr.tid = :tid
            ORDER BY v.rid
            """.trimIndent()
        )
            .bind("tid", tid)
            .mapTo<VulnerabilityWithRepoSqlDto>()
            .list()

        val references = handle.createQuery(
            """
            SELECT vr.vuln_id, vr.url
            FROM vulnerability_references vr
            JOIN vulnerabilities v ON vr.vuln_id = v.vid
            JOIN team_repos tr ON v.rid = tr.rid
            WHERE tr.tid = :tid
            """.trimIndent()
        )
            .bind("tid", tid)
            .mapTo<VulnerabilityReferenceRow>()
            .list()
            .groupBy { it.vulnId }
            .mapValues { (_, refs) -> refs.map { it.url } }

        return rows
            .groupBy { it.repoName }
            .map { (repoName, vulnRows) ->
                TeamVulnerabilities(
                    name            = repoName,
                    vulnerabilities = vulnRows.map { it.toVulnerability(references[it.vid] ?: emptyList()) },
                )
            }
    }

    override fun getTeamSastAlerts(tid: Int): List<TeamSastAlerts> {
        val rows = handle.createQuery(
            """
            SELECT s.sid, s.rid, r.name AS repo_name, s.external_id, s.state, s.rule_id,
                   s.rule_description, s.severity, s.tool_name, s.file_path, s.start_line,
                   s.end_line, s.message, s.html_url, s.platform, s.detected_at, s.updated_at
            FROM sast_alerts s
            JOIN repositories r ON s.rid = r.rid
            JOIN team_repos tr ON s.rid = tr.rid
            WHERE tr.tid = :tid
            ORDER BY s.rid
            """.trimIndent()
        )
            .bind("tid", tid)
            .mapTo<SastAlertWithRepoSqlDto>()
            .list()

        return rows
            .groupBy { it.repoName }
            .map { (repoName, alertRows) ->
                TeamSastAlerts(
                    name   = repoName,
                    alerts = alertRows.map { it.toSastAlert() },
                )
            }
    }
}

