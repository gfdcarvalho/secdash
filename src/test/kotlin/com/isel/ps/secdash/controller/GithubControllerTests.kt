package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.restclient.GithubRestClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [SecdashApplication::class, TestJdbiConfig::class],
    properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class GithubControllerTests : ControllerTestsBase() {

    @MockitoBean
    private lateinit var githubRestClient: GithubRestClient

    @BeforeEach
    fun setup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
            handle.execute("select test_data_for_GithubControllerTests()")
        }
    }

    @AfterAll
    fun cleanUp() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
        }
    }

    @Test
    fun `get repositories returns list when user has github authorization`() {
        whenever(githubRestClient.getRepositoriesFromAuthenticatedUser("testToken"))
            .thenReturn(listOf(
                ExternalRepository(
                    name = "owner/repo",
                    externalId = "123",
                    platform = Platform.GITHUB,
                    externalOwner = ExternalOwner(
                        externalId = "1",
                        name = "owner",
                        url = "https://github.com/owner",
                        avatarUrl = null,
                        platform = Platform.GITHUB,
                    ),
                    htmlUrl = "https://github.com/owner/repo",
                    description = "test repo",
                    issuesCount = 0,
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
                    forksCount = 0,
                    visibility = Repository.Visibility.PUBLIC,
                )
            ))

        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/github/repos")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<ExternalRepository>>()
    }

    @Test
    fun `get repositories returns 401 when user has no github authorization`() {
        val token = login("testUsername2", "testpassword2")

        client().get()
            .uri("/github/repos")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `get repositories returns 401 when request has no auth token`() {
        client().get()
            .uri("/github/repos")
            .exchange()
            .expectStatus().isUnauthorized
    }
}