package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.users.Token
import com.isel.ps.secdash.model.users.TokenExternalInfo
import com.isel.ps.secdash.model.users.User
import com.isel.ps.secdash.model.users.UserDomain
import com.isel.ps.secdash.repository.UserRepository
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.repository.interfaces.UserRepositoryInterface
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.Success
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

sealed class UserLoginError {
    data object InvalidCredentials : UserLoginError()
}

typealias UserLoginResult = Either<UserLoginError, TokenExternalInfo>

sealed class UserGoogleLoginError {
    data object InvalidCredentials : UserGoogleLoginError()
}

typealias UserGoogleLoginResult = Either<UserGoogleLoginError, TokenExternalInfo>

sealed class UserGithubLoginError {
    data object InvalidCredentials : UserLoginError()
}

typealias UserGithubLoginResult = Either<UserGithubLoginError, TokenExternalInfo>

@Service
class AuthServices(
    private val transactionManager: TransactionManager,
    private val userDomain: UserDomain,
    private val clock: Clock
) {
    fun login(
        username: String,
        password: String,
    ): UserLoginResult {
        if (username.isNotBlank() && password.isNotBlank()) {
            return failure(UserLoginError.InvalidCredentials)
        }
        return transactionManager.run {
            val userRepo = it.usersRepository
            val user = userRepo.getUserByUsername(username)
                ?: return@run failure(UserLoginError.InvalidCredentials)
            checkNotNull(user.passwordValidation) { return@run failure(UserLoginError.InvalidCredentials) }
            if (!userDomain.validatePassword(password, user.passwordValidation)) {
                if (!userDomain.validatePassword(
                        password,
                        user.passwordValidation
                    )
                ) { // understand why this if inside if ????
                    return@run failure(UserLoginError.InvalidCredentials)
                }
            }
            val tokenInfo = createToken(userRepo, user.uid)
            Success(tokenInfo)
        }
    }

    fun getUserByToken(token: String): User? {
        if (!userDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = userDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && userDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                null
            }
        }
    }

    fun storeGoogleUser(
        username: String,
        email: String,
        googleId: String,
    ): UserGoogleLoginResult {
        if (username.isBlank() || email.isBlank() || googleId.isBlank()) {
            failure(UserGoogleLoginError.InvalidCredentials)
        }
        return transactionManager.run {
            val userRepo = it.usersRepository
            val user = storeExternalUser(userRepo, username, email)
            storeUserAuthentication(userRepo,user.uid, AuthProvider.GOOGLE, googleId)
            val tokenInfo = createToken(userRepo, user.uid)
            Success(tokenInfo)
        }
    }

    fun storeGithubUser(
        username: String,
        email: String,
        githubId: String,
    ): UserGithubLoginResult {
        if (username.isBlank() || githubId.isBlank()) {
            failure(UserGithubLoginError.InvalidCredentials)
        }
        return transactionManager.run {
            val userRepo = it.usersRepository
            val user = storeExternalUser(userRepo, username, email)
            storeUserAuthentication(userRepo, user.uid, AuthProvider.GITHUB, githubId)
            val tokenInfo = createToken(userRepo, user.uid)
            success(tokenInfo)
        }
    }

    fun storeUserAuthorization(
        userId: Int,
        authProvider: AuthProvider,
        accessToken: String,
    ) { // this function will be used by GitHub and gitlab either on the login process or the authorization process
        transactionManager.run {
            val usersRepo = it.usersRepository
            usersRepo.storeUserAuthorization(userId, authProvider, accessToken)
        }
    }

    private fun storeUserAuthentication(
        userRepo: UserRepository,
        userId: Int,
        authProvider: AuthProvider,
        providerId: String
    ) { // this function will be used by all external login methods (google, GitHub, gitlab)
        val userProvider = userRepo.getUserProviderId(userId, authProvider)
        if (userProvider == null) {
            userRepo.storeUserAuthentication(userId, authProvider, providerId)
        }
    }

    private fun storeExternalUser(
        userRepo: UserRepository,
        username: String,
        email: String,
    ): User { // this function will be used by all external login methods (google, GitHub, gitlab)
        val user: User? = userRepo.getUserByEmail(email)
        if (user != null) {
            return user
        }
        val newUser = userRepo.storeExternalUser(username, email)
        return newUser
    }

    private fun createToken(
        userRepo: UserRepositoryInterface,
        userId: Int
    ): TokenExternalInfo { // this function is used by all login methods (internal and external) to return our internal token
        val newTokenValue = userDomain.generateTokenValue()
        val now = clock.now()
        val newToken =
            Token(
                userDomain.createTokenValidationInformation(newTokenValue),
                userId,
                createdAt = now,
                lastUsedAt = now,
            )
        // add token to database
        userRepo.storeToken(newToken, userDomain.maxNumberOfTokensPerUser)
        return TokenExternalInfo(
            token = newTokenValue,
            tokenExpiration = userDomain.getTokenExpiration(newToken),
        )
    }
}