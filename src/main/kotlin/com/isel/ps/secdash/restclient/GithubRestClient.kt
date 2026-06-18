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
import org.springframework.web.client.toEntity

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

    fun getRepositoriesFromAuthenticatedUser(accessToken: String): List<ExternalRepository> {
        val results = mutableListOf<GithubRepositoryDto>()
        var nextUrl: String? = "https://api.github.com/user/repos?visibility=all&per_page=100"
        while (nextUrl != null) {
            val response = restClient.get()
                .uri(nextUrl)
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .toEntity<Array<GithubRepositoryDto>>()
            response.body?.let { results.addAll(it) }
            nextUrl = response.headers["Link"]
                        ?.firstOrNull()
                        ?.split(",")
                        ?.firstOrNull { it.contains("""rel="next"""") }
                        ?.substringAfter("<")?.substringBefore(">")
        }
        return results.map { it.toExternalGithubRepository() }
    }

    fun getRepositoriesByOwner(owner: String): List<ExternalRepository> {
        val results = mutableListOf<GithubRepositoryDto>()
        var nextUrl: String? = "https://api.github.com/users/$owner/repos?per_page=100"
        while (nextUrl != null) {
            val response = restClient.get()
                .uri(nextUrl)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .toEntity<Array<GithubRepositoryDto>>()
            response.body?.let { results.addAll(it) }
            nextUrl = response.headers["Link"]
                ?.firstOrNull()
                ?.split(",")
                ?.firstOrNull { it.contains("""rel="next"""") }
                ?.substringAfter("<")?.substringBefore(">")
        }
        return results.map { it.toExternalGithubRepository() }
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
        val results = mutableListOf<GithubDependabotAlertDto>()
        var nextUrl: String? = "https://api.github.com/repos/$fullName/dependabot/alerts?per_page=100"
        while (nextUrl != null) {
            val response = restClient.get()
                .uri(nextUrl)
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .toEntity<Array<GithubDependabotAlertDto>>()
            response.body?.let { results.addAll(it) }
            nextUrl = response.headers["Link"]
                ?.firstOrNull()
                ?.split(",")
                ?.firstOrNull { it.contains("""rel="next"""") }
                ?.substringAfter("<")?.substringBefore(">")
        }
        return results.map { it.toExternalVulnerability() }
    }

    fun getSastAlerts(
        fullName: String,
        accessToken: String,
    ): List<ExternalSastAlerts> {
        val results = mutableListOf<GithubSastAlertsDto>()
        var nextUrl: String? = "https://api.github.com/repos/$fullName/code-scanning/alerts?per_page=100"
        while (nextUrl != null) {
            val response = restClient.get()
                .uri(nextUrl)
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .toEntity<Array<GithubSastAlertsDto>>()
            response.body?.let { results.addAll(it) }
            nextUrl = response.headers["Link"]
                ?.firstOrNull()
                ?.split(",")
                ?.firstOrNull { it.contains("""rel="next"""") }
                ?.substringAfter("<")?.substringBefore(">")
        }
        return results.map { it.toExternalSastAlerts() }
    }



    private data class GithubEmail(
        val email: String,
        val primary: Boolean,
        val verified: Boolean,
    )
}