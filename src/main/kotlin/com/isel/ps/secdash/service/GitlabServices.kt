package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.vulnerability.RepositoryVulnerabilities
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.restclient.GitLabRestClient
import com.isel.ps.secdash.service.ResponseTypes.AddRepositoryError
import com.isel.ps.secdash.service.ResponseTypes.AddRepositoryResult
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service

sealed class DependencyScanError {
    data object Unauthorized : DependencyScanError()
    data object NotFound : DependencyScanError()
    data object RepositoryNotFound : DependencyScanError()
}

typealias DependencyScanResult = Either<DependencyScanError, RepositoryVulnerabilities>

@Service
class GitlabServices(
    private val transactionManager: TransactionManager,
    private val gitlabClient: GitLabRestClient,
) {

    fun getRepositoriesByOwner(owner: String): List<ExternalRepository>? {
        return gitlabClient.getRepositoriesByOwner(owner)
    }

    fun addRepository(
        repo: RepositoryCreationDto,
        userId: Int,
    ): AddRepositoryResult {
        if (repo.externalId.isNullOrBlank()) return failure(AddRepositoryError.ExternalIdIsRequired)

        return transactionManager.run {
            val repositoriesRepo = it.repositoriesRepository
            val usersRepo = it.usersRepository
            val accessToken = usersRepo.getAccessToken(userId, AuthProvider.GITLAB) ?: return@run failure(
                AddRepositoryError.UserAuthorizationRequired // maybe we should ask for permission instead of returning error !!!
            )
            val repoToSave =
                if (repo.isComplete()) repo.toExternalRepository(Platform.GITLAB)
                else {
                    val id = repo.externalId.toIntOrNull()
                        ?: return@run failure(AddRepositoryError.InvalidExternalId)
                    gitlabClient.getRepositoryByExternalId(id, accessToken)
                        ?: return@run failure(AddRepositoryError.RepositoryNotFound)
                }

            val repository = repositoriesRepo.storeRepository(userId, repoToSave)

            success(repository)
        }
    }

    fun getDependencyScan(uid: Int, rid: Int): DependencyScanResult {
        return transactionManager.run {
            val userRepo = it.usersRepository
            val repositoriesRepo = it.repositoriesRepository
            val externalId = repositoriesRepo.getExternalId(rid) ?: return@run failure(DependencyScanError.RepositoryNotFound)
            println(externalId)
            if (!repositoriesRepo.userHasAccessToRepository(uid, rid)) return@run failure(DependencyScanError.Unauthorized)
            val accessToken = userRepo.getAccessToken(uid, AuthProvider.GITLAB) ?: return@run failure(DependencyScanError.Unauthorized)
            val vulnerabilities = gitlabClient.getDependencyScan(externalId, accessToken)
            success(RepositoryVulnerabilities(rid, vulnerabilities))
        }
    }
}