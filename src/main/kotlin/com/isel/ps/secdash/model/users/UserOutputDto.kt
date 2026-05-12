package com.isel.ps.secdash.model.users

data class UserOutputDto(
    val uid: Int,
    val name: String,
    val email: String,
    val role: AppRole,
)
