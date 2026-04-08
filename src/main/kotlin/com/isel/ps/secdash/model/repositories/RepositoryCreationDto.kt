package com.isel.ps.secdash.model.repositories

import com.isel.ps.secdash.model.Platform
import java.util.Date

data class RepositoryCreationDto(
    val name: String,
    val externalId: String? = null,
    val externalOwner: ExternalOwner? = null,
    val htmlUrl: String? = null,
    val description: String? = null,
    val issuesCount: Int? = null,
    val createdAt: Date? = null, // talvez tenha que ser ‘String’
    val updatedAt: Date? = null,
    val forksCount: Int? = null,
    val visibility: Repository.Visibility? = null,
) {
    fun toExternalGithubRepository() = ExternalGithubRepository(
        name = name,
        externalId = externalId ?: error("externalId is required"),
        externalOwner = externalOwner ?: error("externalOwner is required"),
        htmlUrl = htmlUrl ?: error("htmlUrl is required"),
        platform = Platform.GITHUB,
        description = description,
        issuesCount = issuesCount ?: error("issuesCount is required"),
        createdAt = createdAt ?: error("createdAt is required"),
        updatedAt = updatedAt ?: error("updatedAt is required"),
        forksCount = forksCount ?: error("forksCount is required"),
        visibility = visibility ?: error("visibility is required"),
    )
}

