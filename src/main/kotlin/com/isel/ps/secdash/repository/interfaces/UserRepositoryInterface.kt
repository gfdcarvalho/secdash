package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.users.PasswordValidationInfo
import com.isel.ps.secdash.model.users.Token
import com.isel.ps.secdash.model.users.TokenExternalInfo
import com.isel.ps.secdash.model.users.TokenValidationInfo
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
        tokenValidationInfo: TokenValidationInfo
    ): Pair<User, Token>?

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun storeExternalUser(
        username: String,
        email: String,
    ): User

    fun storeUserAuthentication(
        userId: Int,
        authProvider: AuthProvider,
        providerId: String,
    )

    fun getUserProviderId(
        userId: Int,
        authProvider: AuthProvider
    ): String?

    fun getAccessToken(
        userId: Int,
        authProvider: AuthProvider
    ): String? // maybe we should have a data class for this return

    fun storeUserAuthorization(
        userId: Int,
        authProvider: AuthProvider,
        accessToken: String
    )

    fun getUserByUsername(username: String): User?

    fun getUserByEmail(email: String): User?

    fun checkIfUserAlreadyExists(email: String): Boolean

    fun checkIfUserExists(uid: Int): Boolean
}