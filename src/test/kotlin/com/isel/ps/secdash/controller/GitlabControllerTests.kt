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
import com.isel.ps.secdash.restclient.GitLabRestClient
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
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.userAuthorizationRequired)
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
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
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
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.ownerIsRequired)
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
            .expectProblem(HttpStatus.NOT_FOUND, Problem.ownerNotFound)
    }

    @Test
    fun `add repository with full repository should return repository`() {
        val token = login("testUsername1", "testpassword1")

        client().post()
            .uri("/gitlab/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(testRepositoryCreationDto(platform = Platform.GITLAB))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<Repository>()
    }

    @Test
    fun `add repository just with externalId should return repository`() {
        val token = login("testUsername1", "testpassword1")
        val externalId = 999
        whenever(gitlabRestClient.getRepositoryByExternalId(externalId, "testToken"))
            .thenReturn(
                testExternalRepository(platform = Platform.GITLAB)
            )

        client().post()
            .uri("/gitlab/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(externalId = externalId.toString()))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<Repository>()
    }

    @Test
    fun `add repository that already exists in our domain should return 400`() {
        val token = login("testUsername1", "testpassword1")
        val externalId = "123"

        client().post()
            .uri("/gitlab/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(externalId = externalId))
            .exchange()
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.repositoryAlreadyAdded)
    }

    @Test
    fun `add repository that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val externalId = 999

        whenever(gitlabRestClient.getRepositoryByExternalId(externalId, token))
            .thenReturn(
                null
            )

        client().post()
            .uri("/gitlab/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(externalId = externalId.toString()))
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `add repository without externalId should return 400`() {
        val token = login("testUsername1", "testpassword1")
        val externalId = " "

        client().post()
            .uri("/gitlab/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(externalId = externalId))
            .exchange()
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.externalIdIsRequired)
    }

    @Test
    fun `add repository without user authorization should return 401`() {
        val token = login("testUsername2", "testpassword2")
        val externalId = "999" // repo that we dont have

        client().post()
            .uri("/gitlab/repositories")
            .header("Authorization", "Bearer $token")
            .bodyValue(RepositoryCreationDto(externalId = externalId))
            .exchange()
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.userAuthorizationRequired)
    }

    @Test
    fun `get Dependency-scanning should return 200`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 1

        whenever(gitlabRestClient.getDependencyScan("testRepository", "testToken"))
            .thenReturn(
                testListExternalVulnerabilities()
            )

        client().get()
            .uri("/gitlab/repositories/$rid/dependency-scanning")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<RepositoryVulnerabilities>()
    }

    @Test
    fun `get Dependency-scanning with invalid rid should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 999 // invalid rid

        client().get()
            .uri("/gitlab/repositories/$rid/dependency-scanning")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `get Dependency-scanning without user authorization should return 401`() {
        val token = login("testUsername2", "testpassword2")
        val rid = 2

        client().get()
            .uri("/gitlab/repositories/$rid/dependency-scanning")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.userAuthorizationRequired)
    }

    @Test
    fun `get Dependency-scanning with user that does not have access to repository should return 401`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 2

        client().get()
            .uri("/gitlab/repositories/$rid/dependency-scanning")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.unauthorized)
    }

    @Test
    fun `get Dependency-scanning from repo that does not have dependabot enabled should return 403`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 1

        whenever(gitlabRestClient.getDependencyScan("123", "testToken"))
            .thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND))

        client().get()
            .uri("/gitlab/repositories/$rid/dependency-scanning")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.repoDoesNotHaveDependabotFeatureEnabled)

    }

    @Test
    fun `get sast should return 200`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 1

        whenever(gitlabRestClient.getSast("123", "testToken"))
            .thenReturn(
                testListExternalSast()
            )

        client().get()
            .uri("/gitlab/repositories/$rid/sast")
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
            .uri("/gitlab/repositories/$rid/sast")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.UNAUTHORIZED, Problem.userAuthorizationRequired)
    }

    @Test
    fun `get sast with invalid rid should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 999 // invalid rid

        client().get()
            .uri("/gitlab/repositories/$rid/sast")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `get sast from repo that does not have sast enabled should return 403`() {
        val token = login("testUsername1", "testpassword1")
        val rid = 1

        whenever(gitlabRestClient.getSast("123", "testToken"))
            .thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND))

        client().get()
            .uri("/gitlab/repositories/$rid/sast")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.repoDoesNotHaveSastFeatureEnabled)
    }

    @Test
    fun `get repo by link should return 200`() {
        val token = login("testUsername1", "testpassword1")
        val link = "https://gitlab.com/testowner/testRepository"

        whenever(gitlabRestClient.getRepositoryByPath("testowner/testRepository"))
            .thenReturn(
                testExternalRepository(platform = Platform.GITLAB)
            )

        client().get()
            .uri("/gitlab/repository?link={link}", link)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<ExternalRepository>()
    }

    @Test
    fun `get repo by link that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val link = "https://gitlab.com/testowner/testRepositoryDoesNotExist"

        whenever(gitlabRestClient.getRepositoryByPath("testowner/testRepositoryDoesNotExist"))
            .thenReturn(
                null
            )

        client().get()
            .uri("/gitlab/repository?link={link}", link)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `get repo by link without user authorization should return 200`() {
        val token = login("testUsername2", "testpassword2")
        val link = "https://gitlab.com/testowner/testRepository"

        whenever(gitlabRestClient.getRepositoryByPath("testowner/testRepository"))
            .thenReturn(
                testExternalRepository(platform = Platform.GITLAB)
            )

        client().get()
            .uri("/gitlab/repository?link={link}", link)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<ExternalRepository>()
    }
}