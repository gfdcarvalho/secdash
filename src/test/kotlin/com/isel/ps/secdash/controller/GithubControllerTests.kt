package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.controller.utils.ControllerTestsBase
import com.isel.ps.secdash.controller.utils.TestJdbiConfig
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.sast.RepositorySast
import com.isel.ps.secdash.model.vulnerability.RepositoryVulnerabilities
import com.isel.ps.secdash.restclient.GithubRestClient
import com.isel.ps.secdash.testExternalRepository
import com.isel.ps.secdash.testListExternalSast
import com.isel.ps.secdash.testListExternalVulnerabilities
import com.isel.ps.secdash.testRepositoryCreationDto
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.client.HttpClientErrorException

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
    fun `get repositories returns list of external repositories`() {
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
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.userAuthorizationRequired)
    }

    @Test
    fun `get repositories returns 404 when no repositories are found`() {
        val token = login("testUsername1", "testpassword1")

        whenever(githubRestClient.getRepositoriesFromAuthenticatedUser("testToken"))
            .thenReturn(null)

        client().get()
            .uri("/github/repos")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
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
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.ownerIsRequired)
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
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.repositoryAlreadyAdded)
    }

    @Test
    fun `add repository that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val name = "testRepositoryDoesNotExist"

        whenever(githubRestClient.getRepositoryByName(name, token))
            .thenReturn(
                null
            )

        client().post()
            .uri("/github/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(name = name))
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `add repository without name should return 400`() {
        val token = login("testUsername1", "testpassword1")
        val name = " "

        client().post()
            .uri("/github/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(name = name))
            .exchange()
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.nameIsRequired)
    }

    @Test
    fun `add repository without user authorization should return 401`() {
        val token = login("testUsername2", "testpassword2")
        val name = "testRepository"

        client().post()
            .uri("/github/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(name = name))
            .exchange()
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.userAuthorizationRequired)
    }

    @Test
    fun `get Dependabot should return 200`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 1

        whenever(githubRestClient.getDependabot("testRepository", "testToken"))
            .thenReturn(
                testListExternalVulnerabilities()
            )

        client().get()
            .uri("/github/repositories/$rid/dependabot")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<RepositoryVulnerabilities>()
    }

    @Test
    fun `get dependabot with invalid rid should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 999 // invalid rid

        client().get()
            .uri("/github/repositories/$rid/dependabot")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `get dependabot without user authorization should return 401`() {
        val token = login("testUsername2", "testpassword2")
        val rid = 2

        client().get()
            .uri("/github/repositories/$rid/dependabot")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.userAuthorizationRequired)
    }

    @Test
    fun `get dependabot with user that does not have access to repository should return 401`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 2

        client().get()
            .uri("/github/repositories/$rid/dependabot")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.unauthorized)
    }

    @Test
    fun `get dependabot from repo that does not have dependabot enabled should return 403`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 1

        whenever(githubRestClient.getDependabot("testRepository", "testToken"))
            .thenThrow(HttpClientErrorException(HttpStatus.FORBIDDEN))

        client().get()
            .uri("/github/repositories/$rid/dependabot")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.repoDoesNotHaveDependabotFeatureEnabled)

    }

    @Test
    fun `get sast should return 200`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 1

        whenever(githubRestClient.getSastAlerts("testRepository", "testToken"))
            .thenReturn(
                testListExternalSast()
            )

        client().get()
            .uri("/github/repositories/$rid/sast")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<RepositorySast>()
    }

    @Test
    fun `get sast without user authorization should return 401`() {
        val token = login("testUsername2", "testpassword2")
        val rid = 2

        client().get()
            .uri("/github/repositories/$rid/sast")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.userAuthorizationRequired)
    }

    @Test
    fun `get sast with invalid rid should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 999 // invalid rid

        client().get()
            .uri("/github/repositories/$rid/sast")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `get sast from repo that does not have sast enabled should return 403`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 1

        whenever(githubRestClient.getSastAlerts("testRepository", "testToken"))
            .thenThrow(HttpClientErrorException(HttpStatus.FORBIDDEN))

        client().get()
            .uri("/github/repositories/$rid/sast")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.repoDoesNotHaveSastFeatureEnabled)
    }
}