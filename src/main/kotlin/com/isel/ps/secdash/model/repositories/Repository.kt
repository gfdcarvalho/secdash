package com.isel.ps.secdash.model.repositories

import com.isel.ps.secdash.model.Owner
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.Repository.Visibility
//import kotlinx.datetime.Instant
import java.sql.Date
import java.time.Instant

//import java.util.Date

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