package com.isel.ps.secdash.model.users

import java.time.Instant

data class UserTokenInfo(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Instant?,
    val needsRefresh: Boolean,
)

data class GitLabTokenResponse(
    @com.fasterxml.jackson.annotation.JsonProperty("access_token")  val accessToken: String,
    @com.fasterxml.jackson.annotation.JsonProperty("refresh_token") val refreshToken: String,
    @com.fasterxml.jackson.annotation.JsonProperty("expires_in")    val expiresIn: Long,
    @com.fasterxml.jackson.annotation.JsonProperty("created_at")    val createdAt: Long,
) {
    val expiresAt: Instant get() = Instant.ofEpochSecond(createdAt + expiresIn)
}
