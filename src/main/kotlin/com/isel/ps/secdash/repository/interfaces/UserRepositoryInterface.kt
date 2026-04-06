package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.users.PasswordValidationInfo
import com.isel.ps.secdash.model.users.Token
import com.isel.ps.secdash.model.users.TokenInfo
import com.isel.ps.secdash.model.users.User
import kotlinx.datetime.Instant

interface UserRepositoryInterface {

    fun createUser(
        username: String,
        email: String,
        password: PasswordValidationInfo,
    ): Int // maybe we should create a dto for this return

    fun storeToken(
        token: Token,
        maxTokens: Int,
    )

    fun getTokenByTokenValidationInfo(
        tokenInfo: TokenInfo
    ): Pair<User, Token>?

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun createGoogleUser(
        username: String,
        email: String,
        googleId: String,
    ): User

    fun getUserByUsername(username: String): User?

    fun getUserByEmail(email: String): User?


}