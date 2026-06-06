package com.isel.ps.secdash.model.teams

data class SimpleTeamsListOutput(
    val teams: List<SimpleTeam>
)

data class SimpleTeam(
    val tid : Int,
    val name : String,
    val description : String?,
)

data class SimpleTeamWithCounts(
    val tid : Int,
    val name : String,
    val description : String?,
    val repoCount: Int,
    val memberCount: Int,
)

data class SimpleTeamWithCountsListOutput(
    val teams : List<SimpleTeamWithCounts>
)