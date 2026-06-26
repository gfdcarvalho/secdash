package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.controller.utils.ControllerTestsBase
import com.isel.ps.secdash.controller.utils.TestJdbiConfig
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryStats
import com.isel.ps.secdash.model.teams.DailySastCount
import com.isel.ps.secdash.model.teams.DailyVulnerabilityCount
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [SecdashApplication::class, TestJdbiConfig::class],
    properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class RepoControllerTests: ControllerTestsBase() {

    @BeforeEach
    fun setup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
            handle.execute("select test_data_for_RepoControllerTests()")
        }
    }

    @AfterAll
    fun cleanup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
        }
    }

    //  getRepo
    @Test
    fun `get repo should return 200 ok with repo`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 1
        client().get()
        .uri("/repos/$repoIdToGet")
        .header("Authorization", "Bearer $token")
        .exchange()
        .expectStatus().isOk
        .expectBody<Repository>()
    }

    @Test
    fun `get repo that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 999
        client().get()
            .uri("/repos/$repoIdToGet")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `get repo with user that does not have access to this repo should return 401`() {
        val token = login("testUsername2", "testpassword2")
        val repoIdToGet = 1
        client().get()
            .uri("/repos/$repoIdToGet")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }

    // deleteRepo
    @Test
    fun `delete repo should return 204 no content`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToDelete = 1
        client().delete()
            .uri("/repos/$repoIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent

        client().get()
            .uri("/repos/$repoIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `delete repo that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToDelete = 999
        client().delete()
            .uri("/repos/$repoIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `delete repo with user that does not have access to this repo should return 403`() {
        val token = login("testUsername2", "testpassword2")
        val repoIdToDelete = 1
        client().delete()
            .uri("/repos/$repoIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }

    // getRepos
    @Test
    fun `get repos should return 200 ok with repo list`() {
        val token = login("testUsername1", "testpassword1")
        client().get()
            .uri("/repos")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<Repository>>()
    }

    @Test
    fun `get repos from user with no repos should return 200 ok with empty repo list`() {
        val token = login("testUsername2", "testpassword2")
        client().get()
            .uri("/repos")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<Repository>>()
    }



    // getRepoStats
    @Test
    fun `get repo stats should return 200 ok with repository stats`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 1
        client().get()
            .uri("/repos/$repoIdToGet/stats")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<RepositoryStats>()
    }

    @Test
    fun `get repo stats from repo with no stats should return 200 ok with empty repository stats`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 2
        client().get()
            .uri("/repos/$repoIdToGet/stats")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<RepositoryStats>()
    }

    @Test
    fun `get repo stats with user that does not have access to this repo should return 403`() {
        val token = login("testUsername2", "testpassword2")
        val repoIdToGet = 1
        client().get()
            .uri("/repos/$repoIdToGet/stats")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }

    @Test
    fun `get repo stats from repo that does not exist should return 404`() {
        val token = login("testUsername2", "testpassword2")
        val repoIdToGet = 999
        client().get()
            .uri("/repos/$repoIdToGet/stats")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    // getRepoVulnerabilitiesHistory
    @Test
    fun `get repo VulnerabilityHistory should return 200 ok with repositories list of daily vulnerabilities count`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 1
        client().get()
            .uri("/repos/$repoIdToGet/vulnerability/history")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<DailyVulnerabilityCount>>()
    }

    @Test
    fun `get repo VulnerabilityHistory from repo that does not have vulnerability history should return empty repositories list of daily vulnerabilities count`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 2
        client().get()
            .uri("/repos/$repoIdToGet/vulnerability/history")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<DailyVulnerabilityCount>>()
    }

    @Test
    fun `get repo VulnerabilityHistory from repo that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 999
        client().get()
            .uri("/repos/$repoIdToGet/vulnerability/history")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `get repo VulnerabilityHistory from repo that user does not have access to should return 403`() {
        val token = login("testUsername2", "testpassword2")
        val repoIdToGet = 1
        client().get()
            .uri("/repos/$repoIdToGet/vulnerability/history")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }




    // getRepoSastHistory
    @Test
    fun `get repo SastHistory should return 200 ok with repositories list of daily sast count`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 1
        client().get()
            .uri("/repos/$repoIdToGet/sast/history")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<DailySastCount>>()
    }

    @Test
    fun `get repo SastHistory from repo that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val repoIdToGet = 999
        client().get()
            .uri("/repos/$repoIdToGet/sast/history")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.repositoryNotFound)
    }

    @Test
    fun `get repo SastHistory from repo that user does not have access to should return 403`() {
        val token = login("testUsername2", "testpassword2")
        val repoIdToGet = 1
        client().get()
            .uri("/repos/$repoIdToGet/sast/history")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }
}