package com.isel.ps.secdash.restclient

import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class GithubRestClient {

    private val restClient = RestClient.create()

    fun fetchGithubEmail(accessToken: String): String? {
        val emails = restClient.get()
            .uri("https://api.github.com/user/emails")
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body(Array<GithubEmail>::class.java)

        return emails?.firstOrNull { it.primary && it.verified }?.email
    }

    private data class GithubEmail(
        val email: String,
        val primary: Boolean,
        val verified: Boolean,
    )
}