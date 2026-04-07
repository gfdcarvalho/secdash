package com.isel.ps.secdash.model

import com.isel.ps.secdash.model.repositories.Repository

class Owner(
    val oid: Int,
    val name : String,
    val url : String,
    val avatarUrl : String?,
    val platform: Platform,
    //val repos : List<Repository>
) {
}