package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.repositories.ExternalGithubRepository
import com.isel.ps.secdash.model.repositories.GithubRepositoryDto
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.restclient.GithubRestClient
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service

sealed class AddRepositoryError {
    data object NameIsRequired : AddRepositoryError()
    data object RepositoryNotFound : AddRepositoryError()
    data object UserGithubAuthorizationRequired : AddRepositoryError() //
}

typealias AddRepositoryResult = Either<AddRepositoryError, Repository> // not sure what to return here ?


@Service
class GithubServices(
    private val transactionManager: TransactionManager,
) {

    private val githubClient: GithubRestClient = GithubRestClient()

    fun getRepositoriesByOwner(owner: String): List<ExternalGithubRepository>? {
        return githubClient.getRepositoriesByOwner(owner)
    }

    fun addRepository(
        repo: RepositoryCreationDto,
        userId: Int,
    ): AddRepositoryResult {
        if (repo.name.isBlank()) return failure(AddRepositoryError.NameIsRequired) // this should never happen

        return transactionManager.run {
            val githubRepo = it.githubRepository
            val usersRepo = it.usersRepository
            val accessToken = usersRepo.getAccessToken(userId, AuthProvider.GITHUB) ?: return@run failure(
                AddRepositoryError.UserGithubAuthorizationRequired // maybe we should ask for permission instead of returning error !!!
            )
            val repoToSave =
                if (repo.isComplete()) repo.toExternalGithubRepository()
                else githubClient.getRepositoryByName(repo.name, accessToken)
                    ?: return@run failure(AddRepositoryError.RepositoryNotFound)

            val repository = githubRepo.storeRepository(userId, repoToSave)

            success(repository)
        }

    }
}