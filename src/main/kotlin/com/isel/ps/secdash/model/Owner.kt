package com.isel.ps.secdash.model

import com.isel.ps.secdash.model.repositories.Repository

class Owner(
    val oid: Int,
    val externalId: String,
    val name : String,
    val url : String,
    val avatarUrl : String?,
    val platform: Platform,
    //val repos : List<Repository>
) {
}