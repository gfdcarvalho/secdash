package com.isel.ps.secdash.restclient

import com.isel.ps.secdash.model.vulnerability.ExternalVulnerability
import com.isel.ps.secdash.model.vulnerability.GithubDependabotAlertDto
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.GithubRepositoryDto
import com.isel.ps.secdash.model.sast.ExternalSastAlerts
import com.isel.ps.secdash.model.sast.GithubSastAlertsDto
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Service
class GithubRestClient {

    private val restClient = RestClient.create()

    fun fetchGithubEmail(accessToken: String): String? {
        val emails = restClient.get()
            .uri("https://api.github.com/user/emails")
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body<Array<GithubEmail>>()

        return emails?.firstOrNull { it.primary && it.verified }?.email
    }

    fun getRepositoriesFromAuthenticatedUser(accessToken: String): List<ExternalRepository>? {
        val  repos = restClient.get()
            .uri("https://api.github.com/user/repos?visibility=all") // podemos passar um parametro para a visibilidade e temos que passar a fazer paginação
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body<Array<GithubRepositoryDto>>()

        return repos?.map { it.toExternalGithubRepository() }
    }

    fun getRepositoriesByOwner(owner: String): List<ExternalRepository>? {
        val  repos = restClient.get()
            .uri("https://api.github.com/users/$owner/repos")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body<Array<GithubRepositoryDto>>()

        return repos?.map { it.toExternalGithubRepository() }
    }

    fun getRepositoryByName(fullName: String, accessToken: String): ExternalRepository? {
        val repo = restClient.get()
            .uri("https://api.github.com/repos/$fullName")
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body<GithubRepositoryDto>()
        return repo?.toExternalGithubRepository()
    }

    fun getDependabot(
        fullName: String,
        accessToken: String,
    ): List<ExternalVulnerability> {
        val alerts = restClient.get()
            .uri("https://api.github.com/repos/$fullName/dependabot/alerts")
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body<Array<GithubDependabotAlertDto>>()
        return alerts?.map { it.toExternalVulnerability() } ?: emptyList()
    }

    fun getSastAlerts(
        fullName: String,
        accessToken: String,
    ): List<ExternalSastAlerts> {
        val alerts = restClient.get()
            .uri("https://api.github.com/repos/$fullName/code-scanning/alerts")
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body<Array<GithubSastAlertsDto>>()
        return alerts?.map { it.toExternalSastAlerts() } ?: emptyList()
    }

    private data class GithubEmail(
        val email: String,
        val primary: Boolean,
        val verified: Boolean,
    )
}