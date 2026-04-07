package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.repositories.GithubRepositoryDto
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.restclient.GithubRestClient
import org.springframework.stereotype.Service

@Service
class GithubServices() {

    private val githubClient: GithubRestClient = GithubRestClient()

    fun getRepositoriesByOwner(owner: String): List<GithubRepositoryDto>? {
        return githubClient.getRepositoriesByOwner(owner)
    }
}