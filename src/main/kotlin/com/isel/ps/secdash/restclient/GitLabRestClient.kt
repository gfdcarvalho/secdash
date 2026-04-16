package com.isel.ps.secdash.restclient

import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.GitlabRepositoryDto
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Service
class GitLabRestClient {
    private val restClient = RestClient.create()

    fun getRepositoriesByOwner(owner: String): List<ExternalRepository>? {
        val repos = restClient.get()
            .uri("https://gitlab.com/api/v4/users/$owner/projects")
            //.header()
            .retrieve()
            .body<Array<GitlabRepositoryDto>>()

            return repos?.map { it.toExternalRepository() }

    }

    fun getRepositoryByExternalId(externalId: Int, accessToken: String): ExternalRepository? {
        val repo = restClient.get()
            .uri("https://gitlab.com/api/v4/projects/$externalId")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body<GitlabRepositoryDto>()

        return repo?.toExternalRepository()
    }

    private fun issuesCount(externalId: Long): Int {
        val response = restClient.get()
            .uri("https://gitlab.com/api/v4/projects/$externalId/issues")
            .retrieve()
            .toEntity(List::class.java)

        return response.headers["X-Total"]?.first()?.toInt() ?: 0
    }

}