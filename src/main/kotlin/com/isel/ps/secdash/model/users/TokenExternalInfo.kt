package com.isel.ps.secdash.model.users

import kotlinx.datetime.Instant

data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant,
)