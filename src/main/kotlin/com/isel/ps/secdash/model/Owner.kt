package com.isel.ps.secdash.model

class Owner(
    val name : String,
    val url : String,
    val avatarUrl : String?,
    val platform: Platform,
    val repos : List<Repository>
) {
}