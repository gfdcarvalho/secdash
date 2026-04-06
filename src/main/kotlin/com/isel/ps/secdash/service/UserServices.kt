package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.users.UserDomain
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

sealed class UserCreationError {
    data object UserAlreadyExists : UserCreationError()
    data object InvalidCredentials : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Int>

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
}