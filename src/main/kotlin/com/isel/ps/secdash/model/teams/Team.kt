package com.isel.ps.secdash.model.teams

import com.isel.ps.secdash.model.repositories.Repository

data class Team(
    val tid: Int,
    val name: String,
    val description: String?,
    val repos: List<Repository>,
    val members: List<TeamMember>
)

data class TeamMember(
    val uid: Int,
    val name: String,
    val email: String,
    val teamRole: TeamRoles,
)


enum class TeamRoles {
    LEADER, COLLABORATOR
}