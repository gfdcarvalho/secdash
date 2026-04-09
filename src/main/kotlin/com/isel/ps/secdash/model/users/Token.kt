package com.isel.ps.secdash.model.users

import kotlinx.datetime.Instant

class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
)