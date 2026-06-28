package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.repositories.CountsBySeverity
import com.isel.ps.secdash.model.repositories.DailySastCount
import com.isel.ps.secdash.model.repositories.DailyVulnerabilityCount
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryStats
import com.isel.ps.secdash.model.users.AppRole
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service
import java.time.ZoneOffset

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

typealias GetRepoVulnerabilityHistoryResult = Either<GetRepositoryError, List<DailyVulnerabilityCount>>

sealed class GetRepoSastHistoryError {
    data object Unauthorized : GetRepoSastHistoryError()
    data object NotFound : GetRepoSastHistoryError()
}

typealias GetRepoSastHistoryResult = Either<GetRepoSastHistoryError, List<DailySastCount>>

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

    fun deleteRepository(rid: Int, userRole: AppRole, uid: Int): DeleteRepositoryResult {
        return transactionManager.run {
            val repo = it.repositoriesRepository

            if (!repo.checkRepositoryExistence(rid)) return@run failure(DeleteRepositoryError.NotFound)
            if (!repo.userHasAccessToRepository(uid, rid) && userRole != AppRole.ADMIN) return@run failure(DeleteRepositoryError.Unauthorized)

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

    fun getRepoVulnerabilityHistory(uid: Int, rid: Int): GetRepoVulnerabilityHistoryResult {
        return transactionManager.run {
            val reposRepo = it.repositoriesRepository

            if (!reposRepo.checkRepositoryExistence(rid)) return@run failure(GetRepositoryError.NotFound)

            if (!reposRepo.userHasAccessToRepository(uid, rid))
                return@run failure(GetRepositoryError.Unauthorized)

            val scans = reposRepo.getRepoVulnerabilityHistory(rid)

            val dailyCounts = scans
                .groupBy { scan ->
                    scan.scannedAt.atZone(ZoneOffset.UTC).toLocalDate()
                }
                .map { (date, scansOnDay) ->

                    val latestScan = scansOnDay.maxBy { it.scannedAt }

                    DailyVulnerabilityCount(
                        date = date,
                        count = latestScan.vulnerabilityCount,
                        countsBySeverity = CountsBySeverity(
                            critical = latestScan.criticalCount,
                            high = latestScan.highCount,
                            medium = latestScan.mediumCount,
                            low = latestScan.lowCount,
                            unknown = latestScan.unknownCount,
                        )
                    )
                }
                .sortedBy { it.date }

            success(dailyCounts)
        }
    }

    fun getRepoSastHistory(uid: Int, rid: Int): GetRepoSastHistoryResult {
        return transactionManager.run {
            val reposRepo = it.repositoriesRepository

            if (!reposRepo.checkRepositoryExistence(rid)) return@run failure(GetRepoSastHistoryError.NotFound)

            if (!reposRepo.userHasAccessToRepository(uid, rid))
                return@run failure(GetRepoSastHistoryError.Unauthorized)

            val scans = reposRepo.getRepoSastHistory(rid)

            val dailyCounts = scans
                .groupBy { scan -> scan.scannedAt.atZone(ZoneOffset.UTC).toLocalDate() }
                .map { (date, scansOnDay) ->
                    val latestScan = scansOnDay.maxBy { it.scannedAt }
                    DailySastCount(
                        date = date,
                        count = latestScan.alertCount,
                        countsBySeverity = CountsBySeverity(
                            critical = latestScan.criticalCount,
                            high     = latestScan.highCount,
                            medium   = latestScan.mediumCount,
                            low      = latestScan.lowCount,
                            unknown  = latestScan.unknownCount,
                        )
                    )
                }
                .sortedBy { it.date }

            success(dailyCounts)
        }
    }

    fun getAllRepositories(): List<Repository> {
        return transactionManager.run {
            it.repositoriesRepository.getAllRepositories()
        }
    }
}