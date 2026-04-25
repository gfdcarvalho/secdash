package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.restclient.GitLabRestClient
import com.isel.ps.secdash.testExternalRepository
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
class GitlabControllerTests: ControllerTestsBase() {

    @MockitoBean
    private lateinit var gitlabRestClient: GitLabRestClient

    @BeforeEach
    fun setup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
            handle.execute("select test_data_for_GitlabControllerTests()")
        }
    }

    @AfterAll
    fun cleanUp() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
        }
    }

    @Test
    fun `get repositories returns list of external repositories`() {
        whenever(gitlabRestClient.getRepositoriesFromAuthenticatedUser("testToken"))
            .thenReturn(listOf(
                testExternalRepository(platform = Platform.GITLAB)
            ))

        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/gitlab/repos")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<List<ExternalRepository>>()
    }

    @Test
    fun `get repositories with no gitlab authorization should return 401`() {
        val token = login("testUsername2", "testpassword2")

        client().get()
            .uri("/gitlab/repos")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `get repositories returns 404 when no repositories are found`() {
        val token = login("testUsername1", "testpassword1")

        whenever(gitlabRestClient.getRepositoriesFromAuthenticatedUser("testToken"))
            .thenReturn(null)

        client().get()
            .uri("/gitlab/repos")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `get repositories by owner should return list of repositories`() {
        val owner = "testowner"
        whenever(gitlabRestClient.getRepositoriesByOwner(owner))
            .thenReturn(listOf(
                testExternalRepository(platform = Platform.GITLAB)
            ))

        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/gitlab/repos/$owner")
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
            .uri("/gitlab/repos/$owner")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `get repositories by owner owner doesn't have repos or does not exist should return 404`() {
        val owner = "testowner"
        whenever(gitlabRestClient.getRepositoriesByOwner(owner))
            .thenReturn(
                null
            )

        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/gitlab/repos/$owner")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }
}