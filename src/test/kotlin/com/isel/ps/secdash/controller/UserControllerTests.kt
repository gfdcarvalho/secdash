package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.model.users.UserCreationModel
import com.isel.ps.secdash.utils.configureWithAppRequirements
import com.isel.ps.secdash.utils.env
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [SecdashApplication::class])
class UserControllerTests {
    companion object {
        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL(env("JDBC_DATABASE_URL"))
                },
            ).configureWithAppRequirements()
    }

    init {
        val schema =
            UserControllerTests::class.java.getResource("/create-schema.sql")?.readText()
                ?: throw IllegalStateException("create-schema.sql not found in test resources")
        jdbi.useHandle<Exception> { handle ->
            handle.connection.createStatement().use { stmt ->
                stmt.execute(schema)
            }
        }
    }

    @BeforeEach
    fun setup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
            handle.execute("select test_data_for_UserControllerTests()")
        }
    }

    @LocalServerPort
    var port: Int = 0

    private fun client() = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

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
        .expectStatus().isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
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
        .expectStatus().isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
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
        .expectStatus().isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
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
        .expectStatus().isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }
}