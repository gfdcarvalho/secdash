package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.users.Token
import com.isel.ps.secdash.model.users.TokenInfo
import com.isel.ps.secdash.model.users.User
import com.isel.ps.secdash.model.users.UserDomain
import com.isel.ps.secdash.model.users.UserOutputDto
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.Success
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service
import kotlin.run

sealed class UserCreationError {
    data object UserAlreadyExists : UserCreationError()
    data object InvalidCredentials : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Int>

sealed class UserLoginError {
    data object InvalidCredentials : UserLoginError()
}

typealias UserLoginResult = Either<UserLoginError, TokenInfo>

sealed class UserGoogleLoginError {
    data object InvalidCredentials : UserLoginError()
}

typealias UserGoogleLoginResult = Either<UserGoogleLoginError, UserOutputDto>

@Service
class UserServices(
    private val transactionManager: TransactionManager,
    private val userDomain: UserDomain,
    private val clock: Clock
) {

    fun createUser(
        username: String,
        email: String,
        password: String,
    ): UserCreationResult {

        // verify the input information (email, password, username) with the user domain functions

        val passwordValidation = userDomain.createPasswordValidationInformation(password)

        return transactionManager.run {
            val userRepo = it.usersRepository

            // TODO()  we first need to check if the user already exists

            val id = userRepo.createUser(username, email, passwordValidation )
            success(id)
        }
    }

    fun login(
        username: String,
        password: String,
    ): UserLoginResult {
        if (username.isNotBlank() || password.isNotBlank()) {
            failure(UserLoginError.InvalidCredentials)
        }
        return transactionManager.run {
            val userRepo = it.usersRepository
            val user = userRepo.getUserByUsername(username)
                ?: return@run failure(UserLoginError.InvalidCredentials)
            checkNotNull(user.passwordValidation) { return@run failure(UserLoginError.InvalidCredentials) }
            if (!userDomain.validatePassword(password, user.passwordValidation)) {
                if (!userDomain.validatePassword(password, user.passwordValidation)) { // understand why this if inside if ????
                    return@run failure(UserLoginError.InvalidCredentials)
                }
            }
            val newTokenValue = userDomain.generateTokenValue()
            val now = clock.now()
            val newToken =
                Token(
                    userDomain.createTokenValidationInformation(newTokenValue),
                    user.uid,
                    createdAt = now,
                    lastUsedAt = now,
                )
            // add token to database
            userRepo.storeToken(newToken, userDomain.maxNumberOfTokensPerUser)

            Success(TokenInfo(newTokenValue))
        }
    }

    fun getUserByToken( token: String): User? {
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
            failure(UserLoginError.InvalidCredentials)
        }
        return transactionManager.run {
            val userRepo = it.usersRepository

            val user: User? = userRepo.getUserByEmail(email)
            if (user != null) {
                success(user.toOutputDto())
            }
            val newUser = userRepo.createGoogleUser(username, email, googleId)
            success(newUser.toOutputDto())
        }

    }
}