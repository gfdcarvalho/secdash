package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.sast.RepositorySast
import com.isel.ps.secdash.model.vulnerability.RepositoryVulnerabilities
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.restclient.GithubRestClient
import com.isel.ps.secdash.service.ResponseTypes.AddRepositoryError
import com.isel.ps.secdash.service.ResponseTypes.AddRepositoryResult
import com.isel.ps.secdash.service.ResponseTypes.GetRepositoriesByOwnerError
import com.isel.ps.secdash.service.ResponseTypes.GetRepositoriesByOwnerResult
import com.isel.ps.secdash.service.ResponseTypes.GetRepositoriesError
import com.isel.ps.secdash.service.ResponseTypes.GetRepositoriesResult
import com.isel.ps.secdash.service.ResponseTypes.SastError
import com.isel.ps.secdash.service.ResponseTypes.SastResult
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service

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


    fun getRepositoriesFromAuthorizedUser(userId: Int): GetRepositoriesResult {
        return transactionManager.run {
            val usersRepo = it.usersRepository
            val accessToken = usersRepo.getAccessToken(userId, AuthProvider.GITHUB) ?: return@run failure(
                GetRepositoriesError.UserAuthorizationIsRequired)
            val repositories = githubClient.getRepositoriesFromAuthenticatedUser(accessToken) ?: return@run failure(
                GetRepositoriesError.RepositoryNotFound
            )
            success(repositories)
        }
    }

    fun getRepositoriesByOwner(owner: String): GetRepositoriesByOwnerResult {
        if (owner.isBlank()) return failure(GetRepositoriesByOwnerError.OwnerIsRequired)
        val repositories = githubClient.getRepositoriesByOwner(owner) ?:
            return failure(GetRepositoriesByOwnerError.OwnerNotFound) // this could be because owner not found or owner doesn't have repos
        return success(repositories)
    }

    fun addRepository(
        repo: RepositoryCreationDto,
        userId: Int,
    ): AddRepositoryResult {
        if (repo.name.isNullOrBlank()) return failure(AddRepositoryError.NameIsRequired) // this should never happen

        return transactionManager.run {
            val repositoriesRepo = it.repositoriesRepository
            val usersRepo = it.usersRepository
            if (repositoriesRepo.userAlreadyHasRepo(userId, repo.name)) return@run failure(AddRepositoryError.RepositoryAlreadyAdded)// not sure if this is the best place to check this !!!
            val accessToken = usersRepo.getAccessToken(userId, AuthProvider.GITHUB) ?: return@run failure(
                AddRepositoryError.UserAuthorizationRequired // maybe we should ask for permission instead of returning error !!!
            )
            val repoToSave =
                if (repo.isComplete()) repo.toExternalRepository(Platform.GITHUB)
                else githubClient.getRepositoryByName(repo.name, accessToken)
                    ?: return@run failure(AddRepositoryError.RepositoryNotFound)
            if (repositoriesRepo.userAlreadyHasRepoByExternalId(userId, repoToSave.externalId, Platform.GITHUB)) return@run failure(
                AddRepositoryError.RepositoryAlreadyAdded)
            val repository = repositoriesRepo.storeRepository(userId, repoToSave)
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
            val fullName = repositoriesRepo.getRepositoryFullName(rid) ?: return@run failure(DependabotError.RepositoryNotFound)
            if (!repositoriesRepo.userHasAccessToRepository(userId, rid)) return@run failure(DependabotError.Unauthorized) // could this return 403 forbidden ??
            val accessToken = userRepo.getAccessToken(userId, AuthProvider.GITHUB) ?: return@run failure(DependabotError.Unauthorized)
            val vulnerabilities = githubClient.getDependabot(fullName, accessToken)
            success(RepositoryVulnerabilities(rid, vulnerabilities))
        }
    }

    fun getSastAlerts(
        userId: Int,
        rid: Int,
    ): SastResult {
        return transactionManager.run {
            val userRepo = it.usersRepository
            val repositoriesRepo = it.repositoriesRepository
            val fullName = repositoriesRepo.getRepositoryFullName(rid) ?: return@run failure(SastError.RepositoryNotFound)
            if (!repositoriesRepo.userHasAccessToRepository(userId, rid)) return@run failure(SastError.Unauthorized)
            val accessToken = userRepo.getAccessToken(userId, AuthProvider.GITHUB) ?: return@run failure(SastError.Unauthorized)
            val externalSastAlerts = githubClient.getSastAlerts(fullName, accessToken)
            success(RepositorySast(rid, externalSastAlerts))
        }
    }
}