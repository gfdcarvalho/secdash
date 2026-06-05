package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.users.UserDomain
import com.isel.ps.secdash.model.users.UserOutputDto
import com.isel.ps.secdash.model.users.UserTeamsAndRepos
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
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

sealed class UserDeletionError {
    data object UserNotFound : UserDeletionError()
    data object Forbidden : UserDeletionError()
}

typealias UserDeletionResult = Either<UserDeletionError, Unit>

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

    fun getUserInformation(uid: Int): UserTeamsAndRepos {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            val repositoryRepo = it.repositoriesRepository
            val teams = teamsRepo.getTeamsByUser(uid)
            val repos = repositoryRepo.findAllByUser(uid)
            UserTeamsAndRepos(repos, teams)
        }
    }

    fun deleteUser(targetUid: Int): UserDeletionResult {
        return transactionManager.run {
            val userRepo = it.usersRepository
            if (!userRepo.checkIfUserExists(targetUid)) return@run Failure(UserDeletionError.UserNotFound)
            userRepo.deleteUser(targetUid)
            success(Unit)
        }
    }

    fun getAllUsers(): List<UserOutputDto> {
        return transactionManager.run {
            it.usersRepository.getAllUsers().map { user -> user.toOutputDto() }
        }
    }
}