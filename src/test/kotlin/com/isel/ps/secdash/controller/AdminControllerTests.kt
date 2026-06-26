package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.controller.utils.ControllerTestsBase
import com.isel.ps.secdash.controller.utils.TestJdbiConfig
import com.isel.ps.secdash.model.users.AppRole
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [SecdashApplication::class, TestJdbiConfig::class],
    properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class AdminControllerTests: ControllerTestsBase() {

    @BeforeEach
    fun setup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
            handle.execute("select test_data_for_AdminControllerTests()")
        }
    }

    @AfterAll
    fun cleanup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
        }
    }
    // delete User
    @Test
    fun `Admin user deletes user should return no content`() {
        val token = login("testUsername1", "testpassword1")
        val userIdToDelete = 2
        client().delete()
            .uri("/admin/delete-user/$userIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `Admin user deletes user that does not exist should return not found`() {
        val token = login("testUsername1", "testpassword1")
        val userIdToDelete = 999
        client().delete()
            .uri("/admin/delete-user/$userIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.UserNotFound)
    }

    @Test
    fun `Normal user tries to delete an user should return Forbidden`() {
        val token = login("testUsername2", "testpassword2")
        val userIdToDelete = 1
        client().delete()
            .uri("/admin/delete-user/$userIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }

    // promote user
    @Test
    fun `Admin promotes normal User should return 200 ok`() {
        val token = login("testUsername1", "testpassword1")
        val userIdToPromote = 1
        client().post()
            .uri("/admin/promote-user/$userIdToPromote")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent

        val promotedUser = adminGetUser(userIdToPromote, token)
        assertNotNull(promotedUser)
        assertEquals(AppRole.ADMIN, promotedUser.role)
    }

    @Test
    fun `Admin promotes User that does not exist should return not found`() {
        val token = login("testUsername1", "testpassword1")
        val userIdToPromote = 999
        client().post()
            .uri("/admin/promote-user/$userIdToPromote")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.UserNotFound)
    }

    @Test
    fun `Normal user tries to promote normal user should return 403 forbidden`() {
        val token = login("testUsername2", "testpassword2")
        val userIdToPromote = 3
        client().post()
            .uri("/admin/promote-user/$userIdToPromote")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }

    // delete team

    @Test
    fun `Admin deletes a Team that he is not a member of should return 204 no content`() {
        val token = login("testUsername1", "testpassword1")
        val teamIdToDelete = 1
        client().delete()
            .uri("/admin/delete-team/$teamIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `Admin deletes Team that does not exist should return not found`() {
        val token = login("testUsername1", "testpassword1")
        val userIdToDelete = 999
        client().delete()
            .uri("/admin/delete-team/$userIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.NOT_FOUND, Problem.teamNotFound)
    }

    @Test
    fun `Normal user tries to delete Team that he is not a member of should return 403 forbidden`() {
        val token = login("testUsername3", "testpassword3")
        val userIdToDelete = 1
        client().delete()
            .uri("/admin/delete-team/$userIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }

    @Test
    fun `Normal user tries to delete Team that he is leader of should return 403 forbidden`() {
        val token = login("testUsername2", "testpassword2")
        val userIdToDelete = 2
        client().delete()
            .uri("/admin/delete-team/$userIdToDelete")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectProblem(HttpStatus.FORBIDDEN, Problem.forbidden)
    }

}