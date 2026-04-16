package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository

interface GitlabRepositoryInterface {
    fun storeRepository(
        userId: Int,
        repository: ExternalRepository,
    ): Repository
}