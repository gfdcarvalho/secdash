package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.repositories.RepositoryWithOwnerSqlDto
import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.repository.interfaces.TeamRepositoryInterface
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class TeamRepository(
    private val handle: Handle,
) : TeamRepositoryInterface {

    override fun getTeamsByUser(uid: Int): List<SimpleTeam> {
        return handle.createQuery(
            """
          SELECT t.tid, t.name, t.description
          FROM team_users tu
          JOIN teams t ON tu.tid = t.tid
          WHERE tu.uid = :uid
          """
        )
            .bind("uid", uid)
            .mapTo<SimpleTeam>()
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

        return Team(name = team.name, description = team.description, repos = repos)
    }


}
