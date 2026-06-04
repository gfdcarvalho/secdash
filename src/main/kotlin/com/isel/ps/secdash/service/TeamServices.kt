package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.model.teams.TeamStats
import com.isel.ps.secdash.model.users.AppRole
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service

sealed class GetTeamsError {

}

typealias GetTeamsResult = Either<GetTeamsError, List<SimpleTeam>>

sealed class GetTeamError {
    data object Unauthorized : GetTeamError()
}

typealias GetTeamResult = Either<GetTeamError, Team>

sealed class CreateTeamError {
    data object InvalidName: CreateTeamError()
    data object InternalError: CreateTeamError()
}

typealias CreateTeamResult = Either<CreateTeamError, Team>

sealed class DeleteTeamError {
    data object OnlyTeamLeaderOrAdmin: DeleteTeamError()
    data object TeamNotFound: DeleteTeamError()
}

typealias DeleteTeamResult = Either<DeleteTeamError, Unit>

sealed class AddUserToTeamError {
    data object OnlyTeamLeader: AddUserToTeamError()
    data object TeamNotFound: AddUserToTeamError()
    data object UserNotFound: AddUserToTeamError()
    data object UserAlreadyOnTeam: AddUserToTeamError()
}

typealias AddUserToTeamResult = Either<AddUserToTeamError, Unit>

sealed class RemoveUserFromTeamError {
    data object OnlyTeamLeader: RemoveUserFromTeamError()
    data object TeamNotFound: RemoveUserFromTeamError()
    data object UserNotFound: RemoveUserFromTeamError()
    data object UserNotOnTeam: RemoveUserFromTeamError()
}

typealias RemoveUserFromTeamResult = Either<RemoveUserFromTeamError, Unit>

sealed class PromoteUserToLeaderError {
    data object OnlyTeamLeader: PromoteUserToLeaderError()
    data object TeamNotFound: PromoteUserToLeaderError()
    data object UserNotOnTeam: PromoteUserToLeaderError()
    data object UserAlreadyLeader: PromoteUserToLeaderError()
}

typealias PromoteUserToLeaderResult = Either<PromoteUserToLeaderError, Unit>

sealed class AddRepositoryToTeamError {
    data object OnlyTeamLeader: AddRepositoryToTeamError()
    data object TeamNotFound: AddRepositoryToTeamError()
    data object RepositoryNotFound: AddRepositoryToTeamError()
    data object RepositoryAlreadyAdded: AddRepositoryToTeamError()
}

typealias AddRepositoryToTeamResult = Either<AddRepositoryToTeamError, Unit>

sealed class RemoveRepoFromTeamError {
    data object OnlyTeamLeader: RemoveRepoFromTeamError()
    data object TeamNotFound: RemoveRepoFromTeamError()
    data object RepositoryNotFound: RemoveRepoFromTeamError()
}

typealias RemoveRepoFromTeamResult = Either<RemoveRepoFromTeamError, Unit>

sealed class GetTeamStatsError {
    data object NotTeamMember: GetTeamStatsError()
    data object TeamNotFound: GetTeamStatsError()
}

typealias GetTeamStatsResult = Either<GetTeamStatsError, TeamStats>

@Service
class TeamServices(
    private val transactionManager: TransactionManager,
) {
    fun getTeamsByUser(uid: Int): GetTeamsResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            success(teamsRepo.getTeamsByUser(uid))
        }
    }

    fun getTeam(
        tid: Int,
        uid: Int,
    ): GetTeamResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamError.Unauthorized)
            val team = teamsRepo.getTeam(tid) ?: TODO()
            success(team)
        }
    }

    fun createTeam(
        uid: Int,
        teamName: String,
        teamDescription: String?,
    ): CreateTeamResult{
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

            if (!teamsRepo.checkUserTeamLeader(uid, tid) && userRole != AppRole.ADMIN) return@run failure(DeleteTeamError.OnlyTeamLeaderOrAdmin)

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

            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamStatsError.NotTeamMember)

            success(TeamStats(
                vulnerabilityStats = teamsRepo.getTeamVulnerabilityStats(tid),
                sastStats = teamsRepo.getTeamSastStats(tid),
            ))
        }
    }

    fun getTeamVulnerabilityHistory(uid: Int, tid: Int): GetTeamVulnerabilityHistoryResult {
        return transactionManager.run {
            val teamsRepo = it.teamRepository

            if (!teamsRepo.checkUserHasTeamAccess(tid, uid)) return@run failure(GetTeamVulnerabilityHistoryError.NotTeamMember)

            success(TeamVulnerabilityHistory(

            ))

        }
}
