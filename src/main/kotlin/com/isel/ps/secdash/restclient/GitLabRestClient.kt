package com.isel.ps.secdash.restclient

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.GitlabRepositoryDto
import com.isel.ps.secdash.model.sast.ExternalSastAlerts
import com.isel.ps.secdash.model.sast.GitlabSastAlertsDto
import com.isel.ps.secdash.model.vulnerability.ExternalVulnerability
import com.isel.ps.secdash.model.vulnerability.GitlabDependencyScanDto
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Service
class GitLabRestClient {
    private val restClient = RestClient.create()
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

    fun getDependencyScan(externalId: String, accessToken: String): List<ExternalVulnerability> {
        val raw = restClient.get()
            .uri("https://gitlab.com/api/v4/projects/$externalId/jobs/artifacts/main/raw/gl-dependency-scanning-report.json?job=dependency_scanning")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body<String>()
            ?: return emptyList()

        val response = objectMapper.readValue<GitlabDependencyScanDto>(raw)
        return response.toExternalVulnerabilities()
    }

    fun getSast(
        externalId: String,
        accessToken: String,
    ): List<ExternalSastAlerts> {
        val raw = restClient.get()
            .uri("https://gitlab.com/api/v4/projects/$externalId/jobs/artifacts/main/raw/gl-sast-report.json?job=semgrep-sast")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body<String>()
            ?: return emptyList()

        val response = objectMapper.readValue<GitlabSastAlertsDto>(raw)
        return response.toExternalSastAlerts()
    }

}