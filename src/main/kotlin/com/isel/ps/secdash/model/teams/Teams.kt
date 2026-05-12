package com.isel.ps.secdash.model.teams

import com.isel.ps.secdash.model.repositories.Repository

data class Teams(
    val name: String,
    val description: String?,
    val repos: List<Repository>
)