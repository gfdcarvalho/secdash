package com.isel.ps.secdash.model.repositories

import com.fasterxml.jackson.annotation.JsonProperty
import com.isel.ps.secdash.model.Platform
import java.sql.Date
import java.time.Instant


data class GithubRepositoryDto(
    val id: Long,
    @JsonProperty("full_name") val name: String,
    @JsonProperty("html_url") val htmlUrl: String,
    val description: String?,
    @JsonProperty("open_issues_count") val openIssuesCount: Int,
    @JsonProperty("created_at") val createdAt: String,
    @JsonProperty("updated_at") val updatedAt: String,
    @JsonProperty("forks_count") val forksCount: Int,
    val visibility: String,
    val owner: GithubOwnerDto,
) {
    fun toExternalGithubRepository() = ExternalGithubRepository(
        name = name,
        externalId = id.toString(),
        platform = Platform.GITHUB,
        externalOwner = owner.toExternalOwner(),
        htmlUrl = htmlUrl,
        description = description ?: "",
        issuesCount = openIssuesCount,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        forksCount = forksCount,
        visibility = when (visibility) {
            "private" -> Repository.Visibility.PRIVATE
            "internal" -> Repository.Visibility.INTERNAL
            else -> Repository.Visibility.PUBLIC
        }
    )
}

data class GithubOwnerDto(
    @JsonProperty("id") val externalId: Long,
    val login: String,
//    val id: Long,
    @JsonProperty("avatar_url") val avatarUrl: String,
    @JsonProperty("html_url") val htmlUrl: String,
) {
    fun toExternalOwner() = ExternalOwner(
        externalId = externalId.toString(),
        name = login,
        url = htmlUrl,
        avatarUrl = avatarUrl,
        platform = Platform.GITHUB
    )
}
