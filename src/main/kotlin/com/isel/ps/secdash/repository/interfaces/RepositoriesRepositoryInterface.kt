package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository

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
}