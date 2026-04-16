package com.isel.ps.secdash.model.repositories

import com.fasterxml.jackson.annotation.JsonProperty
import com.isel.ps.secdash.model.Platform
import java.time.Instant

data class GitlabRepositoryDto(
    val id: Long,
    @JsonProperty("name_with_namespace") val name: String,
    @JsonProperty("web_url") val htmlUrl: String,
    val description: String?,
    //@JsonProperty("open_issues_count") val openIssuesCount: Int,
    @JsonProperty("created_at") val createdAt: String,
    @JsonProperty("last_activity_at") val updatedAt: String,
    @JsonProperty("forks_count") val forksCount: Int,
    val visibility: String,
    val namespace: GitLabNamespaceDto,
) {
    fun toExternalRepository() = ExternalRepository(
        name = name,
        externalId = id.toString(),
        platform = Platform.GITHUB,
        externalOwner = namespace.toExternalOwner(),
        htmlUrl = htmlUrl,
        description = description ?: "",
        issuesCount = 0, // DANGER!!!
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

data class GitLabNamespaceDto(
    @JsonProperty("id") val externalId: Long,
    val name: String,
    @JsonProperty("avatar_url") val avatarUrl: String,
    @JsonProperty("web_url") val htmlUrl: String,
) {
    fun toExternalOwner() = ExternalOwner(
        externalId = externalId.toString(),
        name = name,
        url = htmlUrl,
        avatarUrl = avatarUrl,
        platform = Platform.GITLAB
    )
}


