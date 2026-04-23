package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.restclient.GithubRestClient
import com.isel.ps.secdash.testExternalRepository
import com.isel.ps.secdash.testRepository
import com.isel.ps.secdash.testRepositoryCreationDto
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
                testExternalRepository(platform = Platform.GITHUB)
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

    @Test
    fun `get repositories by owner should return list of repositories`() {
        val owner = "testowner"
        whenever(githubRestClient.getRepositoriesByOwner(owner))
            .thenReturn(listOf(
                testExternalRepository(platform = Platform.GITHUB)
            ))

        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/github/repos/$owner")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<ExternalRepository>>()
    }

    @Test
    fun `get repositories by owner with owner missing should return 400`() {
        val owner = " "
        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/github/repos/$owner")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `add repository with full repository should return repository`() {
        val token = login("testUsername1", "testpassword1")

        client().post()
            .uri("/github/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(testRepositoryCreationDto(platform = Platform.GITHUB))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<Repository>()
    }

    @Test
    fun `add repository just with name should return repository`() {
        val token = login("testUsername1", "testpassword1")
        val name = "testRepo"
        whenever(githubRestClient.getRepositoryByName("testRepo", "testToken"))
            .thenReturn(
                testExternalRepository(platform = Platform.GITHUB)
            )

        client().post()
            .uri("/github/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(name = name))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<Repository>()
    }

    @Test
    fun `add repository that already exists in our domain should return 400`() {
        val token = login("testUsername1", "testpassword1")
        val name = "testRepository"

        client().post()
            .uri("/github/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(name = name))
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }
}