package com.isel.ps.secdash.model.users

import java.time.Instant

data class UserTokenInfo(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Instant?,
)

data class GitLabTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val createdAt: Long,
) {
    val expiresAt: Instant get() = Instant.ofEpochSecond(createdAt + expiresIn)
}
