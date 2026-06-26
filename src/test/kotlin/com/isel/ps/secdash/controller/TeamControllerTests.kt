package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.controller.utils.ControllerTestsBase
import com.isel.ps.secdash.controller.utils.TestJdbiConfig
import com.isel.ps.secdash.model.teams.SimpleTeamWithCounts
import com.isel.ps.secdash.model.teams.SimpleTeamWithCountsListOutput
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.model.teams.TeamCreationInput
import com.isel.ps.secdash.model.teams.TeamUserInput
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [SecdashApplication::class, TestJdbiConfig::class],
    properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class TeamControllerTests: ControllerTestsBase() {

    @BeforeEach
    fun setup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
            handle.execute("select test_data_for_TeamControllerTests()")
        }
    }

    @AfterAll
    fun cleanup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
        }
    }

    // get user teams /teams
    @Test
    fun `get user teams should return list of simple teams`() {
        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/teams")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<SimpleTeamWithCountsListOutput>()
    }

    @Test
    fun `get user teams from user with no teams should return empty list of simple teams`() {
        val token = login("testUsername5", "testpassword5")

        client().get()
            .uri("/teams")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<SimpleTeamWithCountsListOutput>()
    }

    // get team /teams/teamId
    @Test
    fun `get team should return 200 ok with team`() {
        val token = login("testUsername1", "testpassword1")
        val teamId = 1

        client().get()
            .uri("/teams/$teamId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<Team>()
    }

    @Test
    fun `get team with user that does not have access to team should return 403`() {
        val token = login("testUsername5", "testpassword5")
        val teamId = 1

        client().get()
            .uri("/teams/$teamId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }

    @Test
    fun `get team that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val teamId = 999

        client().get()
            .uri("/teams/$teamId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.teamNotFound)
    }

    // post create team /teams
    @Test
    fun `create new team should return 201`() {
        val token = login("testUsername1", "testpassword1")
        val teamToCreate = TeamCreationInput("testNewTeam", "test")

        val result = client().post()
            .uri("/teams")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(teamToCreate)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Team>()
            .returnResult()

        val createdTeam = result.responseBody!!
        assertEquals("/teams/${createdTeam.tid}", result.responseHeaders.location.toString())
    }

    @Test
    fun `create new team with invalid name should return 400`() {
        val token = login("testUsername1", "testpassword1")
        val invalidTeamToCreate = TeamCreationInput("", "test")

        client().post()
            .uri("/teams")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(invalidTeamToCreate)
            .exchange()
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.invalidTeamName)
    }

    @Test
    fun `one user creates more than one team should return 201 for both`() {
        val token = login("testUsername1", "testpassword1")
        val teamToCreate1 = TeamCreationInput("testNewTeam1", "test1")

        val result1 = client().post()
            .uri("/teams")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(teamToCreate1)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Team>()
            .returnResult()

        val createdTeam = result1.responseBody!!
        assertEquals("/teams/${createdTeam.tid}", result1.responseHeaders.location.toString())

        val teamToCreate2 = TeamCreationInput("testNewTeam2", "test2")

        val result2 = client().post()
            .uri("/teams")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(teamToCreate2)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Team>()
            .returnResult()

        val createdTeam2 = result2.responseBody!!
        assertEquals("/teams/${createdTeam2.tid}", result2.responseHeaders.location.toString())
    }

    // delete team /teams/teamId
    @Test
    fun `delete team should return 204 no content`() {
        val token = login("testUsername1", "testpassword1")
        val teamToDelete = 1

        client().delete()
            .uri("/teams/$teamToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent

        client().get()
            .uri("/teams/$teamToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.teamNotFound)
    }

    @Test
    fun `delete team that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val teamToDelete = 999

        client().delete()
            .uri("/teams/$teamToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.teamNotFound)
    }

    @Test
    fun `user that is not team leader tries to delete team should return 403`() {
        val token = login("testUsername2", "testpassword2")
        val teamToDelete = 1

        client().delete()
            .uri("/teams/$teamToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.onlyTeamLeader)
    }

    @Test
    fun `user that is not on the team tries to delete team should return 403`() {
        val token = login("testUsername3", "testpassword3")
        val teamToDelete = 1

        client().delete()
            .uri("/teams/$teamToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.onlyTeamLeader)
    }

    @Test
    fun `user that is not on the team but is admin deletes team should return 204 no content`() {
        val token = login("testUsername1", "testpassword1")
        val teamToDelete = 2

        client().delete()
            .uri("/teams/$teamToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent
    }

    // post add user to team /teams/tid/users
    @Test
    fun `team leader adds user to team should return 200 ok`() {
        val token = login("testUsername1", "testpassword1")
        val teamId = 1
        val userToAdd = TeamUserInput(5)

        client().post()
            .uri("/teams/$teamId/users")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userToAdd)
            .exchange()
            .expectStatus().isOk

        client().get()
            .uri("teams/$teamId")
        .header("Authorization", "Bearer $token")
            .exchange()
        .expectStatus().isOk
        .expectBody<Team>()
            .value { team ->
                assertTrue { team?.members?.any { it.uid == userToAdd.userId } ?: false }
            }
    }

    @Test
    fun `user that is not team leader adds user to team should return 403`() {
        val token = login("testUsername2", "testpassword2")
        val teamId = 1
        val userToAdd = TeamUserInput(5)

        client().post()
            .uri("/teams/$teamId/users")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userToAdd)
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.onlyTeamLeader)
    }

    @Test
    fun `team leader tries to add user that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val teamId = 1
        val userToAdd = TeamUserInput(999)

        client().post()
            .uri("/teams/$teamId/users")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userToAdd)
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.UserNotFound)
    }

    @Test
    fun `user tries to add another user to a team that does not exist should return 404`() {
        val token = login("testUsername1", "testpassword1")
        val teamId = 999
        val userToAdd = TeamUserInput(5)

        client().post()
            .uri("/teams/$teamId/users")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userToAdd)
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.teamNotFound)
    }

    @Test
    fun `team leader tries to add user that is already on the team should return 409`() {
        val token = login("testUsername1", "testpassword1")
        val teamId = 1
        val userToAdd = TeamUserInput(2)

        client().post()
            .uri("/teams/$teamId/users")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userToAdd)
            .exchange()
            .expectProblem(HttpStatus.CONFLICT, Problem.userAlreadyOnTeam)
    }

    @Test
    fun `team leader adds user that is on another team should return 200 ok`() {
        val token = login("testUsername1", "testpassword1")
        val teamId = 1
        val userToAdd = TeamUserInput(3)

        client().post()
            .uri("/teams/$teamId/users")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userToAdd)
            .exchange()
            .expectStatus().isOk

        client().get()
            .uri("teams/$teamId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<Team>()
            .value { team ->
                assertTrue { team?.members?.any { it.uid == userToAdd.userId } ?: false }
            }
    }

    // delete remove user from team /teams/tid/users/uidToRemove
    @Test
    fun `team leader removes user from team should return 204 no content`() {
        val token = login("testUsername1", "testpassword1")
        val teamId = 1
        val userToRemove = 2

        client().delete()
            .uri("/teams/$teamId/users/$userToRemove")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent

        client().get()
            .uri("/teams/$teamId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<Team>()
            .value { team ->
                assertTrue { team?.members?.isNotEmpty() ?: false  }
                assertTrue { team?.members?.none { it.uid == userToRemove } ?: false }
            }
    }

    // pathc makeUserTeamLeader /teams/tid/users/uidTopromote

    // post add repository to Team /teams/tid/repository

    // delete remove repository from team /teams/tid/repository/repoToRemove

    // get team Stats /teams/tid/stats

    // get team Vulnerability history /teams/tid/vulnerability/history

    // get team sast History /teams/tid/sast/history

    // get team vulnerabilities /teams/tid/vulnerabilities

    // get team sast alerts /teams/tid/sast

}