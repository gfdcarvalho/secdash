package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.repositories.ExternalGithubRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.vulnerability.RepositoryVulnerabilities
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

sealed class DependabotError {
    data object Unauthorized : DependabotError()
    data object NotFound : DependabotError()
    data object RepositoryNotFound : DependabotError()
}

typealias DependabotResult = Either<DependabotError, RepositoryVulnerabilities>


@Service
class GithubServices(
    private val transactionManager: TransactionManager,
    private val githubClient: GithubRestClient,
) {

    fun fetchGithubEmail(accessToken: String): String? = githubClient.fetchGithubEmail(accessToken)

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

    fun getDependabot(
        userId: Int,
        rid: Int,
    ): DependabotResult {
        return transactionManager.run {
            val userRepo = it.usersRepository
            val repositoriesRepo = it.repositoriesRepository
            if (!repositoriesRepo.userHasAccessToRepository(userId, rid)) return@run failure(DependabotError.Unauthorized)
            val accessToken = userRepo.getAccessToken(userId, AuthProvider.GITHUB) ?: return@run failure(DependabotError.Unauthorized)
            val fullName = repositoriesRepo.getRepositoryFullName(rid) ?: return@run failure(DependabotError.RepositoryNotFound)
            val vulnerabilities = githubClient.getDependabot(fullName, accessToken)
            success(RepositoryVulnerabilities(rid, vulnerabilities))
        }
    }
}