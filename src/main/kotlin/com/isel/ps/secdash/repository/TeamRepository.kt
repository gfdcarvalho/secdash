package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.model.teams.TeamWithReposSqlDto
import com.isel.ps.secdash.repository.interfaces.TeamRepositoryInterface
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class TeamRepository(
    private val handle: Handle,
) : TeamRepositoryInterface {

    override fun findAllByUser(uid: Int): List<Team> {
        val rows = handle.createQuery(
            """
            SELECT
            t.tid, t.name, t.description,
                
            r.rid, r.name, r.external_id, r.platform, r.owner_id,
            r.html_url, r.description, r.issues_count, r.created_at, r.updated_at, r.forks_count, r.visibility,
    
            o.oid, o.external_id AS o_external_id, o.name AS o_name,o.url,o.avatar_url, o.platform AS o_platform
    
            FROM team_users tu
            JOIN teams t ON tu.tid = t.tid
            LEFT JOIN team_repos tr ON t.tid = tr.tid
            LEFT JOIN repositories r ON tr.rid = r.rid
            LEFT JOIN owners o ON r.owner_id = o.oid
            WHERE tu.uid = :uid
            """
        ).bind("uid", uid)
            .mapTo<TeamWithReposSqlDto>()
            .list()

        return rows
            .groupBy { it.tid }
            .map { (_, teamRows) ->
                val first = teamRows.first()
                Team(
                    name = first.teamName,
                    description = first.teamDescription,
                    repos = teamRows.map { it.toDomain() }
                )
            }
    }
}
