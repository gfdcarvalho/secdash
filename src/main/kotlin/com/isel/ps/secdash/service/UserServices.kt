package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.users.Token
import com.isel.ps.secdash.model.users.TokenInfo
import com.isel.ps.secdash.model.users.UserDomain
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.Success
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

sealed class UserCreationError {
    data object UserAlreadyExists : UserCreationError()
    data object InvalidCredentials : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Int>

sealed class UserLoginError {
    data object InvalidCredentials : UserLoginError()
}

typealias UserLoginResult = Either<UserLoginError, TokenInfo>

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
        if (username.isBlank() || password.isBlank()) {
            failure(UserLoginError.InvalidCredentials)
        }
        return transactionManager.run {
            val userRepo = it.usersRepository
            val user = userRepo.getUserByUsername(username)
                ?: return@run failure(UserLoginError.InvalidCredentials)
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

            Success(TokenInfo(newTokenValue))

        }

    }
}