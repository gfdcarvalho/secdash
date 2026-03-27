package com.isel.ps.secdash.model.users

data class UserCreationDto(
    val username: String,
    val password: String,
    val email: String
)