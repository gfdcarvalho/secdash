package com.isel.ps.secdash.model

import java.util.Date


class Repository(
    val rid: Int,
    val name: String,
    val externalId: String,
    val platform: Platform,
    val owner: Owner,
    val htmlUrl: String,
    val description: String,
    val issuesCount: Int,
    val createdAt: Date,
    val updatedAt: Date,
    val forksCount: Int,
    val visibility: Visibility
    ) {
    enum class Visibility {
        PUBLIC, PRIVATE, INTERNAL
    }
}