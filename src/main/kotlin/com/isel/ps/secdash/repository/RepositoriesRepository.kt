package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.Owner
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositorySqlDto
import com.isel.ps.secdash.model.repositories.RepositorySastHistory
import com.isel.ps.secdash.model.repositories.RepositoryVulnerabilityHistory
import com.isel.ps.secdash.model.repositories.RepositoryWithOwnerSqlDto
import com.isel.ps.secdash.model.repositories.SastStats
import com.isel.ps.secdash.model.repositories.StatRowSqlDto
import com.isel.ps.secdash.model.repositories.VulnerabilityStats
import com.isel.ps.secdash.model.sast.ExternalSastAlerts
import com.isel.ps.secdash.model.sast.SastAlert
import com.isel.ps.secdash.model.sast.SastAlertDetail
import com.isel.ps.secdash.model.sast.SastAlertDetailSqlDto
import com.isel.ps.secdash.model.vulnerability.ExternalVulnerability
import com.isel.ps.secdash.model.vulnerability.Vulnerability
import com.isel.ps.secdash.model.vulnerability.VulnerabilityDetail
import com.isel.ps.secdash.model.vulnerability.VulnerabilityDetailSqlDto
import com.isel.ps.secdash.model.vulnerability.VulnerabilityReferenceRow
import com.isel.ps.secdash.repository.interfaces.RepositoriesRepositoryInterface
import java.time.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class RepositoriesRepository(
    private val handle: Handle,
): RepositoriesRepositoryInterface {

    override fun userHasAccessToRepository(userId: Int, rid: Int): Boolean {
        val resultRid = handle.createQuery(
            "SELECT rid FROM user_repositories WHERE uid = :userId AND rid = :rid"
        )
            .bind("userId", userId)
            .bind("rid", rid)
            .mapTo<Int>()
            .singleOrNull()
        return resultRid != null
    }

    override fun getRepositoryFullName(rid: Int): String? {
        val fullName = handle.createQuery(
            "SELECT name FROM repositories WHERE rid = :rid"
        )
            .bind("rid", rid)
            .mapTo<String>()
            .singleOrNull()
        return fullName
    }

    override fun userAlreadyHasRepo(userId: Int, repoName: String): Boolean {
        val resultRid = handle.createQuery(
            """
            SELECT r.rid FROM repositories r
            JOIN user_repositories ur ON r.rid = ur.rid
            WHERE ur.uid = :userId AND r.name = :repoName
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("repoName", repoName)
            .mapTo<Int>()
            .singleOrNull()
        return resultRid != null
    }

    override fun userAlreadyHasRepoByExternalId(userId: Int, externalId: String, platform: Platform): Boolean {
        val resultRid = handle.createQuery(
            """
            SELECT r.rid FROM repositories r
            JOIN user_repositories ur ON r.rid = ur.rid
            WHERE ur.uid = :userId AND r.external_id = :externalId AND r.platform = :platform::platform
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("externalId", externalId)
            .bind("platform", platform.name)
            .mapTo<Int>()
            .singleOrNull()
        return resultRid != null
    }

    override fun storeRepository(
        userId: Int,
        repository: ExternalRepository
    ): Repository {
        val owner = insertOwner(repository.externalOwner)
        val repositorySqlDto = insertRepository(repository, owner.oid)
        insertIntoUserRepositories(repositorySqlDto.rid, userId)
        return repositorySqlDto.toDomainRepository(owner)
    }

    private fun insertIntoUserRepositories(
        repositoryId: Int,
        userId: Int,
    ) {
        handle.createUpdate(
            """
            insert into user_repositories(uid, rid)
            values (:uid, :rid)
        """.trimIndent()
        )
            .bind("uid", userId)
            .bind("rid", repositoryId)
            .execute()
    }

    private fun insertRepository(
        repository: ExternalRepository,
        ownerId: Int
    ): RepositorySqlDto {
        handle.createUpdate(
            """
                insert into repositories ( name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
                values ( :name, :external_id, :platform::platform, :owner_id, :html_url, :description, :issues_count, :created_at, :updated_at, :forks_count, :visibility)
                ON CONFLICT (external_id, platform) DO NOTHING
            """.trimIndent()
        )
            .bind("name", repository.name)
            .bind("external_id", repository.externalId)
            .bind("platform", repository.platform.name)
            .bind("owner_id", ownerId)
            .bind("html_url", repository.htmlUrl)
            .bind("description", repository.description)
            .bind("issues_count", repository.issuesCount)
            .bind("created_at", repository.createdAt)
            .bind("updated_at", repository.updatedAt)
            .bind("forks_count", repository.forksCount)
            .bind("visibility", repository.visibility)
            .execute()

        val repository = handle.createQuery(
            """
            select * from repositories
            where external_id = :external_id
        """.trimIndent()
        )
            .bind("external_id", repository.externalId)
            .mapTo<RepositorySqlDto>()
            .one()
        return repository
    }

    private fun insertOwner(owner: ExternalOwner): Owner {
        handle.createUpdate(
            """
                insert into owners (external_id, name, url, avatar_url, platform)
                values (:external_id, :name, :url, :avatar_url, :platform)
                ON CONFLICT (external_id, platform) DO NOTHING
            """.trimIndent()
        )
            .bind("external_id", owner.externalId)
            .bind("name", owner.name)
            .bind("url", owner.url)
            .bind("avatar_url", owner.avatarUrl)
            .bind("platform", owner.platform)
            .execute()

        val owner = handle.createQuery(
            """
                select * from owners
                where external_id = :external_id
            """.trimIndent()
        )
            .bind("external_id", owner.externalId)
            .mapTo<Owner>()
            .one()
        return owner
    }

    override fun getExternalId(rid: Int): String? {
        val externalId = handle.createQuery(
        """
            select external_id from repositories
            where rid = :rid
        """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<String>()
            .singleOrNull()

        return externalId
    }

    override fun findAllByUser(uid: Int): List<Repository> {
        return handle.createQuery(
            """
        SELECT
            r.rid, r.name, r.external_id, r.platform, r.owner_id,
            r.html_url, r.description, r.issues_count,
            r.created_at, r.updated_at, r.forks_count, r.visibility,
            o.oid, o.external_id AS o_external_id, o.name AS o_name,
            o.url, o.avatar_url, o.platform AS o_platform
        FROM repositories r
        JOIN user_repositories ur ON r.rid = ur.rid
        JOIN owners o ON r.owner_id = o.oid
        WHERE ur.uid = :uid
        """.trimIndent()
        )
            .bind("uid", uid)
            .mapTo<RepositoryWithOwnerSqlDto>()
            .list()
            .map { it.toDomain() }
    }

    override fun isRepoUsed(rid: Int): Boolean { // podíamos provavelmente juntar isto numa so query com um join, mas por enquanto acho que esta bem
        val repoInTeams = handle.createQuery(
            """
                SELECT EXISTS(SELECT 1 FROM team_repos WHERE rid = :rid)
            """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<Boolean>()
            .one()

        val repoInUsers = handle.createQuery(
            """
                SELECT EXISTS(SELECT 1 FROM user_repositories WHERE rid = :rid)
            """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<Boolean>()
            .one()

        return repoInTeams || repoInUsers
    }

    /**
     * Dangerous!...
     * Deletes a repository and all its associations: team_repos, user_repositories,
     * its vulnerabilities (and their references), SAST alerts and the scan history
     * tables (repo_vulnerability_scans, repo_sast_scans).
     * Should only be called after confirming the repo is no longer used via [isRepoUsed].
     *
     * Child rows must be removed before the repository itself to satisfy the foreign
     * keys; vulnerability_references is removed first since it references vulnerabilities.
     *
     * @param rid the id of the repository to delete
     */
    override fun deleteRepo(rid: Int) {
        handle.createUpdate(
            """
            DELETE FROM vulnerability_references
            WHERE vuln_id IN (SELECT vid FROM vulnerabilities WHERE rid = :rid)
            """.trimIndent(),
        )
            .bind("rid", rid)
            .execute()

        handle.createUpdate("DELETE FROM vulnerabilities WHERE rid = :rid")
            .bind("rid", rid)
            .execute()

        handle.createUpdate("DELETE FROM repo_vulnerability_scans WHERE rid = :rid")
            .bind("rid", rid)
            .execute()

        handle.createUpdate("DELETE FROM sast_alerts WHERE rid = :rid")
            .bind("rid", rid)
            .execute()

        handle.createUpdate("DELETE FROM repo_sast_scans WHERE rid = :rid")
            .bind("rid", rid)
            .execute()

        handle.createUpdate("DELETE FROM team_repos WHERE rid = :rid")
            .bind("rid", rid)
            .execute()

        handle.createUpdate("DELETE FROM user_repositories WHERE rid = :rid")
            .bind("rid", rid)
            .execute()

        handle.createUpdate("DELETE FROM repositories WHERE rid = :rid")
            .bind("rid", rid)
            .execute()
    }

    override fun checkRepositoryExistence(rid: Int): Boolean {
        return handle.createQuery(
            """
                select exists(select 1 from repositories where rid = :rid)
            """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<Boolean>()
            .one()
    }

    override fun getRepositoryById(rid: Int): Repository {
        val repo = handle.createQuery("select * from repositories where rid = :rid")
            .bind("rid", rid)
            .mapTo<RepositorySqlDto>()
            .one()
        val owner = handle.createQuery("select * from owners where oid = :oid")
            .bind("oid", repo.ownerId)
            .mapTo<Owner>()
            .one()
        return repo.toDomainRepository(owner)
    }

    override fun storeVulnerabilities(rid: Int, vulnerabilities: List<ExternalVulnerability>): List<Vulnerability> {
        val now = Instant.now()
        val result = vulnerabilities.map { vuln ->
            val vid = handle.createQuery(
                """
                INSERT INTO vulnerabilities (
                    external_id, title, description, severity, state, cve_id, ghsa_id,
                    package_name, package_version, vulnerable_version_range, fixed_version,
                    manifest_path, cvss_score, cvss_vector, platform, rid, detected_at, updated_at
                ) VALUES (
                    :external_id, :title, :description, :severity::vulnerability_severity,
                    :state::vulnerability_state, :cve_id, :ghsa_id, :package_name,
                    :package_version, :vulnerable_version_range, :fixed_version, :manifest_path,
                    :cvss_score, :cvss_vector, :platform::platform, :rid, :detected_at, :updated_at
                )
                ON CONFLICT (external_id, platform, rid) DO UPDATE SET
                    state      = EXCLUDED.state,
                    severity   = EXCLUDED.severity,
                    title      = EXCLUDED.title,
                    description = EXCLUDED.description,
                    package_version = EXCLUDED.package_version,
                    fixed_version   = EXCLUDED.fixed_version,
                    cvss_score      = EXCLUDED.cvss_score,
                    cvss_vector     = EXCLUDED.cvss_vector,
                    updated_at      = EXCLUDED.updated_at
                RETURNING vid
                """.trimIndent()
            )
                .bind("external_id", vuln.externalId)
                .bind("title", vuln.title)
                .bind("description", vuln.description)
                .bind("severity", vuln.severity.name)
                .bind("state", vuln.state.name)
                .bind("cve_id", vuln.cveId)
                .bind("ghsa_id", vuln.ghsaId)
                .bind("package_name", vuln.packageName)
                .bind("package_version", vuln.packageVersion)
                .bind("vulnerable_version_range", vuln.vulnerableVersionRange)
                .bind("fixed_version", vuln.fixedVersion)
                .bind("manifest_path", vuln.manifestPath)
                .bind("cvss_score", vuln.cvssScore)
                .bind("cvss_vector", vuln.cvssVector)
                .bind("platform", vuln.platform.name)
                .bind("rid", rid)
                .bind("detected_at", vuln.detectedAt ?: now)
                .bind("updated_at", vuln.updatedAt ?: now)
                .mapTo<Int>()
                .one()

            handle.createUpdate("DELETE FROM vulnerability_references WHERE vuln_id = :vid")
                .bind("vid", vid)
                .execute()

            vuln.references.forEach { url ->
                handle.createUpdate("INSERT INTO vulnerability_references (vuln_id, url) VALUES (:vid, :url)")
                    .bind("vid", vid)
                    .bind("url", url)
                    .execute()
            }

            vuln.toVulnerability(vid, rid)
        }

        val severityCounts = result.groupingBy { it.severity }.eachCount()
        handle.createUpdate(
            """
            INSERT INTO repo_vulnerability_scans
                (rid, vulnerability_count, critical_count, high_count, medium_count, low_count, unknown_count)
            VALUES
                (:rid, :count, :critical, :high, :medium, :low, :unknown)
            """.trimIndent()
        )
            .bind("rid", rid)
            .bind("count", result.size)
            .bind("critical", severityCounts[Vulnerability.VulnerabilitySeverity.CRITICAL] ?: 0)
            .bind("high", severityCounts[Vulnerability.VulnerabilitySeverity.HIGH] ?: 0)
            .bind("medium", severityCounts[Vulnerability.VulnerabilitySeverity.MEDIUM] ?: 0)
            .bind("low", severityCounts[Vulnerability.VulnerabilitySeverity.LOW] ?: 0)
            .bind("unknown", severityCounts[Vulnerability.VulnerabilitySeverity.UNKNOWN] ?: 0)
            .execute()

        return result
    }

    override fun storeSastAlerts(rid: Int, sastAlerts: List<ExternalSastAlerts>): List<SastAlert> {
        val result = sastAlerts.map { alert ->
            val sid = handle.createQuery(
                """
                INSERT INTO sast_alerts (
                    rid, external_id, state, severity, rule_id, rule_description,
                    tool_name, file_path, start_line, end_line, message, html_url,
                    platform, detected_at, updated_at
                ) VALUES (
                    :rid, :external_id, :state::sast_state, :severity::sast_severity,
                    :rule_id, :rule_description, :tool_name, :file_path, :start_line,
                    :end_line, :message, :html_url, :platform::platform, :detected_at, :updated_at
                )
                ON CONFLICT (external_id, platform, rid) DO UPDATE SET
                    state            = EXCLUDED.state,
                    severity         = EXCLUDED.severity,
                    rule_description = EXCLUDED.rule_description,
                    file_path        = EXCLUDED.file_path,
                    start_line       = EXCLUDED.start_line,
                    end_line         = EXCLUDED.end_line,
                    message          = EXCLUDED.message,
                    updated_at       = EXCLUDED.updated_at
                RETURNING sid
                """.trimIndent()
            )
                .bind("rid", rid)
                .bind("external_id", alert.externalId)
                .bind("state", alert.state.name)
                .bind("severity", alert.severity.name)
                .bind("rule_id", alert.ruleId)
                .bind("rule_description", alert.ruleDescription)
                .bind("tool_name", alert.toolName)
                .bind("file_path", alert.filePath)
                .bind("start_line", alert.startLine)
                .bind("end_line", alert.endLine)
                .bind("message", alert.message)
                .bind("html_url", alert.htmlUrl)
                .bind("platform", alert.platform.name)
                .bind("detected_at", alert.detectedAt)
                .bind("updated_at", alert.updatedAt)
                .mapTo<Int>()
                .one()

            alert.toSastAlert(sid, rid)
        }

        val severityCounts = result.groupingBy { it.severity }.eachCount()
        handle.createUpdate(
            """
            INSERT INTO repo_sast_scans
                (rid, alert_count, critical_count, high_count, medium_count, low_count, unknown_count)
            VALUES
                (:rid, :count, :critical, :high, :medium, :low, :unknown)
            """.trimIndent()
        )
            .bind("rid", rid)
            .bind("count", result.size)
            .bind("critical", severityCounts[SastAlert.SastSeverity.CRITICAL] ?: 0)
            .bind("high", severityCounts[SastAlert.SastSeverity.HIGH] ?: 0)
            .bind("medium", severityCounts[SastAlert.SastSeverity.MEDIUM] ?: 0)
            .bind("low", severityCounts[SastAlert.SastSeverity.LOW] ?: 0)
            .bind("unknown", severityCounts[SastAlert.SastSeverity.UNKNOWN] ?: 0)
            .execute()

        return result
    }

    override fun getVulnerabilityDetail(vid: Int): VulnerabilityDetail? {
        val dto = handle.createQuery(
            """
            SELECT v.vid, v.rid, v.external_id, v.title, v.description,
                   v.severity, v.state, v.cve_id, v.ghsa_id, v.package_name, v.package_version,
                   v.vulnerable_version_range, v.fixed_version, v.manifest_path,
                   v.cvss_score, v.cvss_vector, v.platform, v.detected_at, v.updated_at,
                   r.name AS repo_name, r.html_url AS repo_html_url,
                   o.name AS owner_name, o.avatar_url AS owner_avatar_url
            FROM vulnerabilities v
            JOIN repositories r ON v.rid = r.rid
            JOIN owners o ON r.owner_id = o.oid
            WHERE v.vid = :vid
            """.trimIndent()
        )
            .bind("vid", vid)
            .mapTo<VulnerabilityDetailSqlDto>()
            .findOne()
            .orElse(null) ?: return null

        val references = handle.createQuery(
            "SELECT vuln_id, url FROM vulnerability_references WHERE vuln_id = :vid"
        )
            .bind("vid", vid)
            .mapTo<VulnerabilityReferenceRow>()
            .list()
            .map { it.url }

        return dto.toVulnerabilityDetail(references)
    }

    override fun userHasAccessToVulnerability(uid: Int, vid: Int): Boolean {
        return handle.createQuery(
            """
            SELECT EXISTS (
                SELECT 1 FROM vulnerabilities v
                JOIN user_repositories ur ON v.rid = ur.rid
                WHERE v.vid = :vid AND ur.uid = :uid
                UNION
                SELECT 1 FROM vulnerabilities v
                JOIN team_repos tr ON v.rid = tr.rid
                JOIN team_users tu ON tr.tid = tu.tid
                WHERE v.vid = :vid AND tu.uid = :uid
            )
            """.trimIndent()
        )
            .bind("vid", vid)
            .bind("uid", uid)
            .mapTo<Boolean>()
            .one()
    }

    override fun getSastAlertDetail(sid: Int): SastAlertDetail? {
        val dto = handle.createQuery(
            """
            SELECT s.sid, s.rid, s.external_id, s.state, s.rule_id, s.rule_description,
                   s.severity, s.tool_name, s.file_path, s.start_line, s.end_line,
                   s.message, s.html_url, s.platform, s.detected_at, s.updated_at,
                   r.name AS repo_name, r.html_url AS repo_html_url,
                   o.name AS owner_name, o.avatar_url AS owner_avatar_url
            FROM sast_alerts s
            JOIN repositories r ON s.rid = r.rid
            JOIN owners o ON r.owner_id = o.oid
            WHERE s.sid = :sid
            """.trimIndent()
        )
            .bind("sid", sid)
            .mapTo<SastAlertDetailSqlDto>()
            .findOne()
            .orElse(null) ?: return null

        return dto.toSastAlertDetail()
    }

    override fun userHasAccessToSastAlert(uid: Int, sid: Int): Boolean {
        return handle.createQuery(
            """
            SELECT EXISTS (
                SELECT 1 FROM sast_alerts s
                JOIN user_repositories ur ON s.rid = ur.rid
                WHERE s.sid = :sid AND ur.uid = :uid
                UNION
                SELECT 1 FROM sast_alerts s
                JOIN team_repos tr ON s.rid = tr.rid
                JOIN team_users tu ON tr.tid = tu.tid
                WHERE s.sid = :sid AND tu.uid = :uid
            )
            """.trimIndent()
        )
            .bind("sid", sid)
            .bind("uid", uid)
            .mapTo<Boolean>()
            .one()
    }

    override fun getRepoVulnerabilityStats(rid: Int): VulnerabilityStats {
        val rows = handle.createQuery(
            """
        SELECT state, severity, COUNT(*) AS count
        FROM vulnerabilities
        WHERE rid = :rid
        GROUP BY state, severity
        """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<StatRowSqlDto>()
            .list()

        return VulnerabilityStats.from(rows)

    }

    override fun getRepoSastStats(rid: Int): SastStats {
        val rows = handle.createQuery(
            """
        SELECT state, severity, COUNT(*) AS count
        FROM sast_alerts
        WHERE rid = :rid
        GROUP BY state, severity
        """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<StatRowSqlDto>()
            .list()

        return SastStats.from(rows)
    }

    override fun getRepoVulnerabilityHistory(rid: Int): List<RepositoryVulnerabilityHistory> {
        return handle.createQuery(
            """
        SELECT scan_id, rid, scanned_at, vulnerability_count,
               critical_count, high_count, medium_count, low_count, unknown_count
        FROM repo_vulnerability_scans
        WHERE rid = :rid
        ORDER BY scanned_at
        """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<RepositoryVulnerabilityHistory>()
            .list()
    }

    override fun getRepoSastHistory(rid: Int): List<RepositorySastHistory> {
        return handle.createQuery(
            """
            SELECT scan_id, rid, scanned_at, alert_count,
                   critical_count, high_count, medium_count, low_count, unknown_count
            FROM repo_sast_scans
            WHERE rid = :rid
            ORDER BY scanned_at
            """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<RepositorySastHistory>()
            .list()
    }

    override fun getAllRepositories(): List<Repository> {
        return handle.createQuery(
            """
        SELECT
            r.rid, r.name, r.external_id, r.platform, r.owner_id,
            r.html_url, r.description, r.issues_count,
            r.created_at, r.updated_at, r.forks_count, r.visibility,
            o.oid, o.external_id AS o_external_id, o.name AS o_name,
            o.url, o.avatar_url, o.platform AS o_platform
        FROM repositories r
        JOIN owners o ON r.owner_id = o.oid
        """.trimIndent()
        )
            .mapTo<RepositoryWithOwnerSqlDto>()
            .list()
            .map { it.toDomain() }
    }
}