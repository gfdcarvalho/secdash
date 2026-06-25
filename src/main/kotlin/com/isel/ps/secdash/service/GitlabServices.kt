package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.sast.RepositorySast
import com.isel.ps.secdash.model.vulnerability.RepositoryVulnerabilities
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.restclient.GitLabRestClient
import com.isel.ps.secdash.service.responseTypes.AddRepositoryError
import com.isel.ps.secdash.service.responseTypes.AddRepositoryResult
import com.isel.ps.secdash.service.responseTypes.GetRepoByLinkResult
import com.isel.ps.secdash.service.responseTypes.GetRepositoriesByOwnerError
import com.isel.ps.secdash.service.responseTypes.GetRepositoriesByOwnerResult
import com.isel.ps.secdash.service.responseTypes.GetRepositoriesError
import com.isel.ps.secdash.service.responseTypes.GetRepositoriesResult
import com.isel.ps.secdash.service.responseTypes.SastError
import com.isel.ps.secdash.service.responseTypes.SastResult
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.Instant

sealed class DependencyScanError {
    data object Unauthorized : DependencyScanError()
    data object NotFound : DependencyScanError()
    data object UserAuthorizationIsRequired : DependencyScanError()
    data object RepositoryNotFound : DependencyScanError()
    data object RepoDoesNotHaveDependancyScanFeatureEnabled : DependencyScanError()
}

typealias DependencyScanResult = Either<DependencyScanError, RepositoryVulnerabilities>

@Service
class GitlabServices(
    private val transactionManager: TransactionManager,
    private val gitlabClient: GitLabRestClient,
) {

    fun getValidToken(userId: Int): String? {
        val tokenInfo = transactionManager.run {
            it.usersRepository.getTokenInfo(userId, AuthProvider.GITLAB)
        } ?: return null

        if (!tokenInfo.needsRefresh) {
            return tokenInfo.accessToken
        }

        val refreshToken = tokenInfo.refreshToken ?: return null
        val newTokens = try {
            gitlabClient.refreshToken(refreshToken)
        } catch (e: Exception) {
            return null
        }

        transactionManager.run {
            it.usersRepository.storeUserAuthorization(
                userId       = userId,
                authProvider = AuthProvider.GITLAB,
                accessToken  = newTokens.accessToken,
                refreshToken = newTokens.refreshToken,
                expiresAt    = newTokens.expiresAt,
            )
        }

        return newTokens.accessToken
    }

    fun getRepositoriesFromAuthorizedUser(userId: Int): GetRepositoriesResult {
        return transactionManager.run {
            val usersRepo = it.usersRepository
            val accessToken = usersRepo.getAccessToken(userId, AuthProvider.GITLAB) ?: return@run failure(
                GetRepositoriesError.UserAuthorizationIsRequired)
            try {
                val repositories = gitlabClient.getRepositoriesFromAuthenticatedUser(accessToken) ?: return@run failure(
                    GetRepositoriesError.RepositoryNotFound
                )
                success(repositories)
            } catch (e: HttpClientErrorException) {
                when (e.statusCode) {
                    HttpStatus.NOT_FOUND -> failure(GetRepositoriesError.RepositoryNotFound)
                    HttpStatus.UNAUTHORIZED -> failure(GetRepositoriesError.Unauthorized)
                    else -> failure(GetRepositoriesError.Unauthorized)
                }
            }

        }
    }

    fun getRepositoriesByOwner(owner: String): GetRepositoriesByOwnerResult {
        if (owner.isBlank()) return failure(GetRepositoriesByOwnerError.OwnerIsRequired)
        val repositories = gitlabClient.getRepositoriesByOwner(owner) ?:
        return failure(GetRepositoriesByOwnerError.OwnerNotFound) // this could be because owner not found or owner doesn't have repos
        return success(repositories)
    }

    fun addRepository(
        repo: RepositoryCreationDto,
        userId: Int,
    ): AddRepositoryResult {
        if (repo.externalId.isNullOrBlank()) return failure(AddRepositoryError.ExternalIdIsRequired)

        return transactionManager.run {
            val repositoriesRepo = it.repositoriesRepository
            val usersRepo = it.usersRepository
            if (repositoriesRepo.userAlreadyHasRepoByExternalId(userId, repo.externalId, Platform.GITLAB)) return@run failure(AddRepositoryError.RepositoryAlreadyAdded)
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

    fun  getDependencyScan(uid: Int, rid: Int): DependencyScanResult {
        return transactionManager.run {
            val repositoriesRepo = it.repositoriesRepository
            val externalId = repositoriesRepo.getExternalId(rid) ?: return@run failure(DependencyScanError.RepositoryNotFound)
            if (!repositoriesRepo.userHasAccessToRepository(uid, rid)) return@run failure(DependencyScanError.Unauthorized)
            val accessToken = getValidToken(uid) ?: return@run failure(DependencyScanError.UserAuthorizationIsRequired)
            val externalVulnerabilities = try {
                gitlabClient.getDependencyScan(externalId, accessToken)
            } catch(e: HttpClientErrorException){
                return@run when (e.statusCode) {
                    HttpStatus.NOT_FOUND -> failure(DependencyScanError.RepoDoesNotHaveDependancyScanFeatureEnabled)
                    HttpStatus.UNAUTHORIZED -> failure(DependencyScanError.Unauthorized)
                    else -> failure(DependencyScanError.Unauthorized)
                }
            }
            val vulnerabilities = repositoriesRepo.storeVulnerabilities(rid, externalVulnerabilities)
            success(RepositoryVulnerabilities(rid, vulnerabilities))
        }
    }

    fun getSast(
        userId: Int,
        rid: Int,
    ): SastResult {
        return transactionManager.run {
            val repositoriesRepo = it.repositoriesRepository
            val externalId = repositoriesRepo.getExternalId(rid) ?: return@run failure(SastError.RepositoryNotFound)
            if (!repositoriesRepo.userHasAccessToRepository(userId, rid)) return@run failure(SastError.Unauthorized)
            val accessToken = getValidToken(userId) ?: return@run failure(SastError.UserAuthorizationIsRequired)
            val externalSastAlerts = try {
                gitlabClient.getSast(externalId, accessToken)
            }catch (e: HttpClientErrorException){
                return@run when (e.statusCode) {
                    HttpStatus.NOT_FOUND -> failure(SastError.RepoDoesNotHaveSastFeatureEnabled)
                    else -> failure(SastError.Unauthorized)
                }
            }
            val sastAlerts = repositoriesRepo.storeSastAlerts(rid, externalSastAlerts)
            success(RepositorySast(rid, sastAlerts))
        }
    }

    fun getRepoByLink(
        link: String,
        uid: Int,
    ): GetRepoByLinkResult {
        TODO()
    }

}