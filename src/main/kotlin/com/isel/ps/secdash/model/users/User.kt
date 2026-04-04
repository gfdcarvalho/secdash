package com.isel.ps.secdash.model.users

data class User(
    val uid: Int,
    val name : String,
    val email : String,
    val passwordValidation : String?,
//    val googleId: String?  // we may need this in the future
) {
    fun toOutputDto() = UserOutputDto(
        uid = this.uid,
        name = this.name,
        email = this.email,
    )
}