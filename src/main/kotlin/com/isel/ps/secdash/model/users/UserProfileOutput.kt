package com.isel.ps.secdash.model.users

import com.isel.ps.secdash.model.repositories.ExternalRepository

data class UserProfileOutput(
    val username: String,
    val email: String,
    val repos: List<ExternalRepository>
)