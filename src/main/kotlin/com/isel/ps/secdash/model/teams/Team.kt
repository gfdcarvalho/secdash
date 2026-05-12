package com.isel.ps.secdash.model.teams

import com.isel.ps.secdash.model.Owner
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.Repository
import java.time.Instant

data class Team(
    val name: String,
    val description: String?,
    val repos: List<Repository>
)

data class TeamWithReposSqlDto(
    val tid: Int,
    val teamName: String,
    val teamDescription: String?,

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
    val visibility: Repository.Visibility,

    val oid: Int,
    val oExternalId: String,
    val oName: String,
    val url: String,
    val avatarUrl: String,
    val oPlatform: Platform,
) {

    fun toDomain(): Repository {
        return Repository(
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
}