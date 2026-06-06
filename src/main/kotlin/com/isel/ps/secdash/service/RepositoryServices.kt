package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryStats
import com.isel.ps.secdash.model.sast.RepositorySast
import com.isel.ps.secdash.model.teams.TeamStats
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.service.responseTypes.GetTeamSastAlertsError
import com.isel.ps.secdash.service.responseTypes.GetTeamStatsError
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service

sealed class GetRepositoryError{
    data object NotFound : GetRepositoryError()
    data object Unauthorized : GetRepositoryError()
}

typealias GetRepositoryResult = Either<GetRepositoryError, Repository>

sealed class DeleteRepositoryError{
    data object Unauthorized : DeleteRepositoryError()
    data object NotFound : DeleteRepositoryError()
}

typealias DeleteRepositoryResult = Either<DeleteRepositoryError, Unit>

typealias GetRepoStatsResult = Either<GetRepositoryError, RepositoryStats>

@Service
class RepositoryServices(
    private val transactionManager: TransactionManager,
) {
    fun findAllByUser(uid: Int): List<Repository> {
        return transactionManager.run {
            val reposRepo = it.repositoriesRepository
            reposRepo.findAllByUser(uid)
        }
    }

    fun getRepositoryById(
        rid: Int,
        uid: Int
    ): GetRepositoryResult {
        return transactionManager.run {
            val repositoryRepo = it.repositoriesRepository

            if (!repositoryRepo.checkRepositoryExistence(rid)) return@run failure(GetRepositoryError.NotFound)
            if (!repositoryRepo.userHasAccessToRepository(uid, rid)) return@run failure(GetRepositoryError.Unauthorized)

            val repo = repositoryRepo.getRepositoryById(rid)
            success(repo)
        }
    }

    fun deleteRepository(rid: Int, uid: Int): DeleteRepositoryResult {
        return transactionManager.run {
            val repo = it.repositoriesRepository

            if (!repo.checkRepositoryExistence(rid)) return@run failure(DeleteRepositoryError.NotFound)
            if (!repo.userHasAccessToRepository(uid, rid)) return@run failure(DeleteRepositoryError.Unauthorized)

            repo.deleteRepo(rid)
            success(Unit)
        }
    }

    fun getRepoStats(uid: Int, rid: Int): GetRepoStatsResult {
        return transactionManager.run {
            val reposRepo = it.repositoriesRepository

            if(!reposRepo.checkRepositoryExistence(rid)) return@run failure(GetRepositoryError.NotFound)
            if (!reposRepo.userHasAccessToRepository(uid, rid)) return@run failure(GetRepositoryError.Unauthorized)

            success(RepositoryStats(
                vulnerabilityStats = reposRepo.getRepoVulnerabilityStats(rid),
                sastStats = reposRepo.getRepoSastStats(rid),
            ))
        }
    }
}