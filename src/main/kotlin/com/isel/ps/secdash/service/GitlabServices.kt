package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.repository.JdbiTransactionManager
import com.isel.ps.secdash.restclient.GitLabRestClient
import com.isel.ps.secdash.service.ResponseTypes.AddRepositoryError
import com.isel.ps.secdash.service.ResponseTypes.AddRepositoryResult
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service

@Service
class GitlabServices(
    //private val transactionManager: TransactionManager,
    private val gitlabClient: GitLabRestClient,
    private val transactionManager: JdbiTransactionManager
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
}