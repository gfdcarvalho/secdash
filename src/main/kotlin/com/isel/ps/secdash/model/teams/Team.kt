package com.isel.ps.secdash.model.teams

import com.isel.ps.secdash.model.repositories.Repository

data class Team(
    val tid: Int,
    val name: String,
    val description: String?,
    val repos: List<Repository>
)


enum class TeamRoles {
    LEADER, COLLABORATOR
}