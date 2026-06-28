package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.teams.CountsBySeverity
import com.isel.ps.secdash.model.teams.DailySastCount
import com.isel.ps.secdash.model.teams.DailyVulnerabilityCount
import com.isel.ps.secdash.model.teams.SimpleTeamWithCounts
import com.isel.ps.secdash.model.teams.SimpleTeamWithCountsListOutput
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.model.teams.TeamStats
import com.isel.ps.secdash.model.users.AppRole
import java.time.ZoneOffset
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.service.responseTypes.AddRepositoryToTeamError
import com.isel.ps.secdash.service.responseTypes.AddRepositoryToTeamResult
import com.isel.ps.secdash.service.responseTypes.AddUserToTeamError
import com.isel.ps.secdash.service.responseTypes.AddUserToTeamResult
import com.isel.ps.secdash.service.responseTypes.CreateTeamError
import com.isel.ps.secdash.service.responseTypes.CreateTeamResult
import com.isel.ps.secdash.service.responseTypes.DeleteTeamError
import com.isel.ps.secdash.service.responseTypes.DeleteTeamResult
import com.isel.ps.secdash.service.responseTypes.GetTeamError
import com.isel.ps.secdash.service.responseTypes.GetTeamResult
import com.isel.ps.secdash.service.responseTypes.GetTeamSastAlertsError
import com.isel.ps.secdash.service.responseTypes.GetTeamSastAlertsResult
import com.isel.ps.secdash.service.responseTypes.GetTeamSastHistoryError
import com.isel.ps.secdash.service.responseTypes.GetTeamSastHistoryResult
import com.isel.ps.secdash.service.responseTypes.GetTeamStatsError
import com.isel.ps.secdash.service.responseTypes.GetTeamStatsResult
import com.isel.ps.secdash.service.responseTypes.GetTeamVulnerabilitiesError
import com.isel.ps.secdash.service.responseTypes.GetTeamVulnerabilitiesResult
import com.isel.ps.secdash.service.responseTypes.GetTeamVulnerabilityHistoryError
import com.isel.ps.secdash.service.responseTypes.GetTeamVulnerabilityHistoryResult
import com.isel.ps.secdash.service.responseTypes.PromoteUserToLeaderError
import com.isel.ps.secdash.service.responseTypes.PromoteUserToLeaderResult
import com.isel.ps.secdash.service.responseTypes.RemoveRepoFromTeamError
import com.isel.ps.secdash.service.responseTypes.RemoveRepoFromTeamResult
import com.isel.ps.secdash.service.responseTypes.RemoveUserFromTeamError
import com.isel.ps.secdash.service.responseTypes.RemoveUserFromTeamResult
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service

@Service
class TeamServices(
    private val transactionManager: TransactionManager,
) {
    fun getTeamsByUser(uid: Int): SimpleTeamWithCountsListOutput {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            SimpleTeamWithCountsListOutput(teamsRepo.getTeamsByUser(uid))
        }
    }

    fun getTeam(
        tid: Int,
        uid: Int,
    ): GetTeamResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            val team = teamsRepo.getTeam(tid) ?: return@run failure(GetTeamError.TeamNotFound)
            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamError.Unauthorized)
            success(team)
        }
    }

    fun createTeam(
        uid: Int,
        teamName: String,
        teamDescription: String?,
    ): CreateTeamResult {
        if (teamName.isBlank()) return failure(CreateTeamError.InvalidName)

        return transactionManager.run {
            val teamsRepo = it.teamRepository

            val tid = teamsRepo.createTeam(uid, teamName, teamDescription)
            val team = teamsRepo.getTeam(tid) ?: return@run failure(CreateTeamError.InternalError)
            success(team)
        }
    }

    fun deleteTeam(
        uid: Int,
        userRole: AppRole,
        tid: Int,
    ): DeleteTeamResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            val reposRepository = it.repositoriesRepository
            val team = teamsRepo.getTeam(tid) ?: return@run failure(DeleteTeamError.TeamNotFound)

            if (!teamsRepo.checkUserTeamLeader(uid, tid) && userRole != AppRole.ADMIN) return@run failure(
                DeleteTeamError.OnlyTeamLeaderOrAdmin)

            teamsRepo.deleteTeam(tid)

            team.repos.forEach { repo ->
                if (!reposRepository.isRepoUsed(repo.rid)){
                    reposRepository.deleteRepo(repo.rid)
                }
            }

            success(Unit)
        }
    }

    fun addUserToTeam(
        uid: Int,
        tid: Int,
        userToAdd: Int,
    ): AddUserToTeamResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            val usersRepo = it.usersRepository

            // check if team exists
            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(AddUserToTeamError.TeamNotFound)

            // check if is team leader
            if (!teamsRepo.checkUserTeamLeader(uid, tid)) return@run failure(AddUserToTeamError.OnlyTeamLeader)

            // check if user exists
            if (!usersRepo.checkIfUserExists(userToAdd)) return@run failure(AddUserToTeamError.UserNotFound)

            // check if user is not already on the team
            if (teamsRepo.checkUserAlreadyOnTeam(tid, userToAdd)) return@run failure(AddUserToTeamError.UserAlreadyOnTeam)

            // add user to team
            teamsRepo.addUserToTeam(tid, userToAdd)

            success(Unit)
        }
    }

    fun removeUserFromTeam(
        uid: Int,
        tid: Int,
        userToRemove: Int,
    ): RemoveUserFromTeamResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            val usersRepo = it.usersRepository

            // check if team exists
            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(RemoveUserFromTeamError.TeamNotFound)

            //check if is team leader
            if (!teamsRepo.checkUserTeamLeader(uid, tid)) return@run failure(RemoveUserFromTeamError.OnlyTeamLeader)

            // check if user exists
            if (!usersRepo.checkIfUserExists(userToRemove)) return@run failure(RemoveUserFromTeamError.UserNotFound)

            // check if user is on team
            if (!teamsRepo.checkUserAlreadyOnTeam(tid, userToRemove)) return@run failure(RemoveUserFromTeamError.UserNotOnTeam)

            // remove user from team
            teamsRepo.removeUserFromTeam(tid, userToRemove)

            success(Unit)
        }
    }

    fun promoteUserToLeader(
        uid: Int,
        tid: Int,
        userToPromote: Int,
    ): PromoteUserToLeaderResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(PromoteUserToLeaderError.TeamNotFound)

            if (!teamsRepo.checkUserTeamLeader(uid, tid)) return@run failure(PromoteUserToLeaderError.OnlyTeamLeader)

            if (!teamsRepo.checkUserAlreadyOnTeam(tid, userToPromote)) return@run failure(PromoteUserToLeaderError.UserNotOnTeam)

            if (teamsRepo.checkUserTeamLeader(userToPromote, tid)) return@run failure(PromoteUserToLeaderError.UserAlreadyLeader)

            teamsRepo.promoteUserToLeader(tid, userToPromote)

            success(Unit)
        }
    }

    fun addRepositoryToTeam(
        uid: Int,
        tid: Int,
        repositoryToAdd: Int,
    ): AddRepositoryToTeamResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            val reposRepo = it.repositoriesRepository

            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(AddRepositoryToTeamError.TeamNotFound)

            if (!teamsRepo.checkUserTeamLeader(uid, tid)) return@run failure(AddRepositoryToTeamError.OnlyTeamLeader)

            // check if repo exists
            if (!reposRepo.checkRepositoryExistence(repositoryToAdd)) return@run failure(AddRepositoryToTeamError.RepositoryNotFound)

            // check if repo is already added
            if (teamsRepo.checkTeamHasRepo(tid, repositoryToAdd)) return@run failure(AddRepositoryToTeamError.RepositoryAlreadyAdded)

            // add repo
            teamsRepo.addRepositoryToTeam(tid, repositoryToAdd)

            success(Unit)
        }
    }

    fun removeRepositoryFromTeam(
        uid: Int,
        tid: Int,
        repositoryToRemove: Int,
    ): RemoveRepoFromTeamResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(RemoveRepoFromTeamError.TeamNotFound)

            if (!teamsRepo.checkUserTeamLeader(uid, tid)) return@run failure(RemoveRepoFromTeamError.OnlyTeamLeader)

            if (!teamsRepo.checkTeamHasRepo(tid, repositoryToRemove)) return@run failure(RemoveRepoFromTeamError.RepositoryNotFound)

            teamsRepo.removeRepositoryFromTeam(tid, repositoryToRemove)

            success(Unit)
        }
    }

    fun getTeamStats(
        uid: Int,
        tid: Int,
    ): GetTeamStatsResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(GetTeamStatsError.TeamNotFound)

            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamStatsError.NotTeamMember)

            success(TeamStats(
                vulnerabilityStats = teamsRepo.getTeamVulnerabilityStats(tid),
                sastStats = teamsRepo.getTeamSastStats(tid),
                repoCounts = teamsRepo.getTeamRepoProblemCounts(tid),
            ))
        }
    }

    fun getTeamVulnerabilityHistory(uid: Int, tid: Int): GetTeamVulnerabilityHistoryResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(GetTeamVulnerabilityHistoryError.TeamNotFound)

            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamVulnerabilityHistoryError.NotTeamMember)

            val scans = teamsRepo.getTeamVulnerabilityHistory(tid)
            val dailyCounts = scans
                .groupBy { scan -> scan.scannedAt.atZone(ZoneOffset.UTC).toLocalDate() }
                .map { (date, scansOnDay) ->
                    val lastPerRepo = scansOnDay
                        .groupBy { scan -> scan.rid }
                        .values
                        .map { repoScans -> repoScans.maxBy { scan -> scan.scannedAt } }
                    DailyVulnerabilityCount(
                        date = date,
                        count = lastPerRepo.sumOf { it.vulnerabilityCount },
                        countsBySeverity = CountsBySeverity(
                            critical = lastPerRepo.sumOf { it.criticalCount },
                            high     = lastPerRepo.sumOf { it.highCount },
                            medium   = lastPerRepo.sumOf { it.mediumCount },
                            low      = lastPerRepo.sumOf { it.lowCount },
                            unknown  = lastPerRepo.sumOf { it.unknownCount },
                        ),
                    )
                }
                .sortedBy { it.date }

            success(dailyCounts)
        }
    }

    fun getTeamSastHistory(uid: Int, tid: Int): GetTeamSastHistoryResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(GetTeamSastHistoryError.TeamNotFound)

            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamSastHistoryError.NotTeamMember)

            val scans = teamsRepo.getTeamSastHistory(tid)
            val dailyCounts = scans
                .groupBy { scan -> scan.scannedAt.atZone(ZoneOffset.UTC).toLocalDate() }
                .map { (date, scansOnDay) ->
                    val lastPerRepo = scansOnDay
                        .groupBy { scan -> scan.rid }
                        .values
                        .map { repoScans -> repoScans.maxBy { scan -> scan.scannedAt } }
                    DailySastCount(
                        date = date,
                        count = lastPerRepo.sumOf { it.alertCount },
                        countsBySeverity = CountsBySeverity(
                            critical = lastPerRepo.sumOf { it.criticalCount },
                            high     = lastPerRepo.sumOf { it.highCount },
                            medium   = lastPerRepo.sumOf { it.mediumCount },
                            low      = lastPerRepo.sumOf { it.lowCount },
                            unknown  = lastPerRepo.sumOf { it.unknownCount },
                        ),
                    )
                }
                .sortedBy { it.date }

            success(dailyCounts)
        }
    }

    fun getTeamVulnerabilities(uid: Int, tid: Int): GetTeamVulnerabilitiesResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(GetTeamVulnerabilitiesError.TeamNotFound)

            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamVulnerabilitiesError.NotTeamMember)

            success(teamsRepo.getTeamVulnerabilities(tid))
        }
    }

    fun getTeamSastAlerts(uid: Int, tid: Int): GetTeamSastAlertsResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            if (!teamsRepo.checkTeamExistence(tid)) return@run failure(GetTeamSastAlertsError.TeamNotFound)

            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamSastAlertsError.NotTeamMember)

            success(teamsRepo.getTeamSastAlerts(tid))
        }
    }

    fun getAllTeams(): SimpleTeamWithCountsListOutput {
        return transactionManager.run {
            SimpleTeamWithCountsListOutput(it.teamRepository.getAllTeams())
        }
    }
}
