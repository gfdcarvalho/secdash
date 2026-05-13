package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.teams.SimpleTeam
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import org.springframework.stereotype.Service

@Service
class TeamServices(
    private val transactionManager: TransactionManager
) {
    fun getTeamsByUser(uid: Int): List<SimpleTeam> {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            teamsRepo.getTeamsByUser(uid)
        }
    }

    fun getTeam(
        tid: Int,
        uid: Int,
    ): Team {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            // check if user has access to this team

            val team = teamsRepo.getTeam(tid) ?: TODO()
            team
        }
    }
}
