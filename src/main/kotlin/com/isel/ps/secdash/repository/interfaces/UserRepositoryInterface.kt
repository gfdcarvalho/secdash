package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.users.PasswordValidationInfo
import com.isel.ps.secdash.model.users.User

interface UserRepositoryInterface {

    fun createUser(
        username: String,
        email: String,
        password: PasswordValidationInfo,
    ): Int // maybe we should create a dto for this return

    fun createGoogleUser(
        username: String,
        email: String,
        googleId: String,
    ): User

    fun getUserByUsername(username: String): User?

    fun getUserByEmail(email: String): User?

    fun storeToken(
        token: String,
        maxTokens: Int,
    )
}