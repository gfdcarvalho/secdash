package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.controller.utils.ControllerTestsBase
import com.isel.ps.secdash.controller.utils.TestJdbiConfig
import com.isel.ps.secdash.model.users.UserCreationModel
import com.isel.ps.secdash.model.users.UserOutputDto
import com.isel.ps.secdash.model.users.UserProfileInformation
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // for the @AfterAll annotation
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [SecdashApplication::class, TestJdbiConfig::class],
    properties = ["spring.main.allow-bean-definition-overriding=true"], // so we can use the new jdbi bean for the test database
)
class UserControllerTests : ControllerTestsBase() {
    @BeforeEach
    fun setup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
            handle.execute("select test_data_for_UserControllerTests()")
        }
    }

    @AfterAll
    fun cleanUp() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
        }
    }

    @Test
    fun `Create a new User through register function`() {
        val username    = "newtestUsername"
        val password    = "newtestPassword"
        val email       = "newtestEmail@test.com"
        client().post()
            .uri("/users/register")
            .bodyValue(UserCreationModel(username, password, email))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectHeader().location("/users/me")
    }

    @Test
    fun `Create new User with invalid name should fail`() {
        val username    = ""
        val password    = "newtestPassword"
        val email       = "newtestEmail@test.com"
        client().post()
            .uri("/users/register")
            .bodyValue(UserCreationModel(username, password, email))
            .exchange()
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.invalidUsername)
    }

    @Test
    fun `Create new User with invalid email should fail`() {
        val username    = "newtestUsername"
        val password    = "newtestPassword"
        val email       = ""
        client().post()
            .uri("/users/register")
            .bodyValue(UserCreationModel(username, password, email))
            .exchange()
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.invalidEmail)
    }

    @Test
    fun `Create new User with invalid password should fail`() {
        val username    = "newtestUsername"
        val password    = ""
        val email       = "newtestEmail@test.com"
        client().post()
            .uri("/users/register")
            .bodyValue(UserCreationModel(username, password, email))
            .exchange()
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.invalidPassword)
    }

    @Test
    fun `Create User that already exists should fail`() {
        val username    = "testUsername1"
        val password    = "testPassword1"
        val email       = "testemail@test1.com"
        client().post()
            .uri("/users/register")
            .bodyValue(UserCreationModel(username, password, email))
            .exchange()
            .expectProblem(HttpStatus.BAD_REQUEST, Problem.userAlreadyExists)
    }

    @Test
    fun `get logged in user information should return 200`(){
        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/users/me")
            .header("Authorization", "Bearer $token")
        .exchange()
        .expectStatus().isOk
        .expectBody<UserProfileInformation>()
    }

    @Test
    fun `get all users should return 200`(){
        val token = login("testUsername1", "testpassword1")

        client().get()
            .uri("/users")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<UserOutputDto>>()
            .value { list ->
                assertTrue { list?.isNotEmpty() ?: false }
            }
    }
}