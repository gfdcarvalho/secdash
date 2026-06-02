package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.teams.SastStats
import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.model.teams.VulnerabilityStats

interface TeamRepositoryInterface {
    fun getTeamsByUser(uid: Int): List<SimpleTeam>

    fun getTeam(teamId: Int): Team?

    fun checkUserHasTeamAccess(tid: Int, uid: Int): Boolean

    fun createTeam(uid: Int, teamName: String, description: String?): Int

    fun checkTeamExistence(tid: Int): Boolean

    fun checkUserTeamLeader(uid: Int, tid: Int): Boolean

    fun deleteTeam(tid: Int)

    fun checkUserAlreadyOnTeam(tid: Int, uid: Int): Boolean

    fun addUserToTeam(tid: Int, userToAdd: Int)

    fun removeUserFromTeam(tid: Int, userToRemove: Int)

    fun promoteUserToLeader(tid: Int, userToPromote: Int)

    fun checkTeamHasRepo(tid: Int, rid: Int): Boolean

    fun addRepositoryToTeam(tid: Int, repositoryToAdd: Int)

    fun removeRepositoryFromTeam(tid: Int, repositoryToRemove: Int)

    fun getTeamVulnerabilityStats(tid: Int): VulnerabilityStats

    fun getTeamSastStats(tid: Int): SastStats
}