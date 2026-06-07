package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryVulnerabilityHistory
import com.isel.ps.secdash.model.repositories.SastStats
import com.isel.ps.secdash.model.repositories.VulnerabilityStats
import com.isel.ps.secdash.model.sast.ExternalSastAlerts
import com.isel.ps.secdash.model.sast.SastAlert
import com.isel.ps.secdash.model.sast.SastAlertDetail
import com.isel.ps.secdash.model.vulnerability.ExternalVulnerability
import com.isel.ps.secdash.model.vulnerability.Vulnerability
import com.isel.ps.secdash.model.vulnerability.VulnerabilityDetail

interface RepositoriesRepositoryInterface {

    fun userHasAccessToRepository(
        userId: Int,
        rid: Int
    ): Boolean

    fun getRepositoryFullName(rid: Int): String?

    fun storeRepository(
        userId: Int,
        repository: ExternalRepository,
    ): Repository

    fun userAlreadyHasRepo(userId: Int, repoName: String): Boolean

    fun userAlreadyHasRepoByExternalId( userId: Int, externalId: String, platform: Platform): Boolean

    fun getExternalId(rid: Int): String?

    fun findAllByUser(uid: Int): List<Repository>

    fun isRepoUsed(rid: Int): Boolean

    fun deleteRepo(rid: Int)

    fun checkRepositoryExistence(rid: Int): Boolean

    fun getRepositoryById(rid: Int): Repository

    fun storeVulnerabilities(rid: Int, vulnerabilities: List<ExternalVulnerability>): List<Vulnerability>

    fun storeSastAlerts(rid: Int, sastAlerts: List<ExternalSastAlerts>): List<SastAlert>

    fun getVulnerabilityDetail(vid: Int): VulnerabilityDetail?

    fun userHasAccessToVulnerability(uid: Int, vid: Int): Boolean

    fun getSastAlertDetail(sid: Int): SastAlertDetail?

    fun userHasAccessToSastAlert(uid: Int, sid: Int): Boolean

    fun getRepoVulnerabilityStats(rid: Int): VulnerabilityStats

    fun getRepoSastStats(rid: Int): SastStats

    fun getRepoVulnerabilityHistory(rid: Int): List<RepositoryVulnerabilityHistory>
}