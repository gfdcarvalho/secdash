package com.isel.ps.secdash.repository.interfaces

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
}