package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.Owner
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositorySqlDto
import com.isel.ps.secdash.repository.interfaces.GithubRepositoryInterface
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class GithubRepository(
    private val handle: Handle,
) : GithubRepositoryInterface {


}