package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import org.springframework.stereotype.Service

@Service
class TeamServices(
    private val transactionManager: TransactionManager
) {
    fun findAllByUser(uid: Int): List<Team> {
        return transactionManager.run {
            val teamsRepo = it.teamRepository
            teamsRepo.findAllByUser(uid)
        }
    }
}