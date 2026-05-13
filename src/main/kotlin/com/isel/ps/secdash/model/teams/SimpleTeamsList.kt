package com.isel.ps.secdash.model.teams

data class SimpleTeamsListOutput(
    val teams: List<SimpleTeam>
)

data class SimpleTeam(
    val id : Int,
    val name : String,
    val description : String?,
)