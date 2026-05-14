package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.Team

interface TeamRepositoryInterface {
    fun getTeamsByUser(uid: Int): List<SimpleTeam>

    fun getTeam(teamId: Int): Team?

    fun checkUserHasTeamAccess(tid: Int, uid: Int): Boolean

    fun createTeam(uid: Int, teamName: String, description: String?): Int

    fun checkTeamExistence(tid: Int): Boolean

    fun checkUserTeamLeader(uid: Int, tid: Int): Boolean

    fun deleteTeam(tid: Int)
}