package com.isel.ps.secdash.model.repositories

import com.isel.ps.secdash.model.Platform
import java.time.Instant

//import java.util.Date

data class ExternalRepository(
    val name: String,
    val externalId: String,
    val platform: Platform,
    val externalOwner: ExternalOwner,
    val htmlUrl: String,
    val description: String?,
    val issuesCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val forksCount: Int,
    val visibility: Repository.Visibility,
)


data class ExternalOwner(
    val externalId: String,
    val name : String,
    val url : String,
    val avatarUrl: String?,
    val platform: Platform
)