package com.isel.ps.secdash.model.users

import kotlinx.datetime.Instant

class Token(
    val tokenInfo: TokenInfo,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
)