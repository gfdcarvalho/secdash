package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.users.UserDomain
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.Either.Left
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

sealed class UserCreationError {
    data object UserAlreadyExists : UserCreationError()
    data object InvalidCredentials : UserCreationError()
    data object InvalidUsername : UserCreationError()
    data object InvalidEmail : UserCreationError()
    data object InvalidPassword : UserCreationError()
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

        if (!userDomain.validUsername(username)) return Failure(UserCreationError.InvalidUsername)
        if (!userDomain.validEmail(email)) return Failure(UserCreationError.InvalidEmail)
        if (!userDomain.validPassword(password)) return Failure(UserCreationError.InvalidPassword)

        val passwordValidation = userDomain.createPasswordValidationInformation(password)

        return transactionManager.run {
            val userRepo = it.usersRepository
            if (userRepo.checkIfUserAlreadyExists(email)) return@run Failure(UserCreationError.UserAlreadyExists)
            val id = userRepo.createUser(username, email, passwordValidation )
            success(id)
        }
    }
}