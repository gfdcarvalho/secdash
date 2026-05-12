package com.isel.ps.secdash.model.repositories

import com.isel.ps.secdash.model.Owner
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.Repository.Visibility
import java.time.Instant

class Repository(
    val rid: Int,
    val name: String,
    val externalId: String,
    val platform: Platform,
    val owner: Owner,
    val htmlUrl: String,
    val description: String,
    val issuesCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val forksCount: Int,
    val visibility: Visibility
    ) {
    enum class Visibility {
        PUBLIC, PRIVATE, INTERNAL
    }
}



data class RepositorySqlDto(
    val rid: Int,
    val name: String,
    val externalId: String,
    val platform: Platform,
    val ownerId: Int,
    val htmlUrl: String,
    val description: String,
    val issuesCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val forksCount: Int,
    val visibility: Visibility
) {
    fun toDomainRepository(owner: Owner) = Repository(
        rid = rid,
        name = name,
        externalId = externalId,
        platform = platform,
        owner = owner,
        htmlUrl = htmlUrl,
        description = description,
        issuesCount = issuesCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        forksCount = forksCount,
        visibility = visibility,
    )
}

data class RepositoryWithOwnerSqlDto(
    val rid: Int,
    val name: String,
    val externalId: String,
    val platform: Platform,
    val ownerId: Int,
    val htmlUrl: String,
    val description: String?,
    val issuesCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val forksCount: Int,
    val visibility: Visibility,
    val oid: Int,
    val oExternalId: String,
    val oName: String,
    val url: String,
    val avatarUrl: String?,
    val oPlatform: Platform,
) {
    fun toDomain() = Repository(
        rid = rid,
        name = name,
        externalId = externalId,
        platform = platform,
        owner = Owner(
            oid = oid,
            externalId = oExternalId,
            name = oName,
            url = url,
            avatarUrl = avatarUrl,
            platform = oPlatform,
        ),
        htmlUrl = htmlUrl,
        description = description ?: "",
        issuesCount = issuesCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        forksCount = forksCount,
        visibility = visibility,
    )
}