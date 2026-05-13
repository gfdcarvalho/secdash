package com.isel.ps.secdash.service

import com.isel.ps.secdash.controller.model.Problem.Companion.unauthorized
import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.Team
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

@Service
class TeamServices(
    private val transactionManager: TransactionManager
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
}
