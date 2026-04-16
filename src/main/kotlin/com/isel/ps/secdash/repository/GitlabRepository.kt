package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.repository.interfaces.GitlabRepositoryInterface
import org.jdbi.v3.core.Handle

class GitlabRepository(
    private val handle: Handle,
) : GitlabRepositoryInterface {
    override fun storeRepository(
        userId: Int,
        repository: ExternalRepository
    ): Repository {
        TODO("Not yet implemented")
    }

}