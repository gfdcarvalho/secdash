package com.isel.ps.secdash.model.users

import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.SimpleTeamWithCounts

class UserProfileInformation(
    val id: Int,
    val name: String,
    val email: String,
    val role: AppRole,
    val repositories: List<Repository>,
    val teams: List<SimpleTeamWithCounts>,
)

data class UserTeamsAndRepos(
    val repositories: List<Repository>,
    val teams: List<SimpleTeamWithCounts>,
)