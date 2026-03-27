package com.isel.ps.secdash.model.users

data class User(
    val uid: Int,
    val name : String,
    val email : String,
    val password : String,
) {
}