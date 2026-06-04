package com.isel.ps.secdash.scheduler

import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.service.GithubServices
import com.isel.ps.secdash.service.GitlabServices
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScanScheduler(
    private val transactionManager: TransactionManager,
    private val githubServices: GithubServices,
    private val gitlabServices: GitlabServices,
) {
    private val log = LoggerFactory.getLogger(ScanScheduler::class.java)

    @Scheduled(fixedDelay = 5 * 60 * 1000) // corre de 5 em 5 minutos
    fun scanTeams() {
        log.info("Scanning teams...")
        val teams = transactionManager.run {
            it.teamRepository.getTeamsForScan(limit = 10)
        }

        if (teams.isEmpty()) return

        log.info("Scanning ${teams.size} team(s)")

        teams.forEach { target ->
            val team = transactionManager.run {
                it.teamRepository.getTeam(target.tid)
            } ?: return@forEach

            team.repos.forEach { repo ->
                when (repo.platform) {
                    Platform.GITHUB -> {
                        githubServices.getDependabot(target.leaderUid, repo.rid)
                        githubServices.getSastAlerts(target.leaderUid, repo.rid)
                    }
                    Platform.GITLAB -> {
                        gitlabServices.getDependencyScan(target.leaderUid, repo.rid)
                        gitlabServices.getSast(target.leaderUid, repo.rid)
                    }
                }
            }

            transactionManager.run {
                it.teamRepository.updateLastScanAt(target.tid)
            }

            log.info("Scan complete for team ${target.tid}")
        }
    }
}