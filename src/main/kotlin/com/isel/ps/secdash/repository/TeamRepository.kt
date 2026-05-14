package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.repositories.RepositoryWithOwnerSqlDto
import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.model.teams.TeamRoles
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

        return Team(tid= team.tid, name = team.name, description = team.description, repos = repos)
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
}
