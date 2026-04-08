package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.repositories.ExternalGithubRepository
import com.isel.ps.secdash.model.repositories.Repository

interface GithubRepositoryInterface {

    fun storeRepository(
        userId: Int,
        repository: ExternalGithubRepository,
    ): Repository
}