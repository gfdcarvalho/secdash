package com.isel.ps.secdash.controller.utils

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.users.UserLoginDto
import com.isel.ps.secdash.model.users.UserOutputDto
import com.isel.ps.secdash.model.users.UserTokenOutputModel
import com.isel.ps.secdash.utils.configureWithAppRequirements
import com.isel.ps.secdash.utils.env
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.awt.PageAttributes
import kotlin.test.assertEquals

abstract class ControllerTestsBase {
    companion object {
        private val _jdbi: Jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(env("JDBC_DATABASE_TEST_URL"))
            },
        ).configureWithAppRequirements()
    }

    protected val jdbi: Jdbi = _jdbi

    init {
        val schema = ControllerTestsBase::class.java.getResource("/create-schema.sql")?.readText()
            ?: throw IllegalStateException("create-schema.sql not found in test resources")
        jdbi.useHandle<Exception> { handle ->
            handle.connection.createStatement().use { stmt ->
                stmt.execute(schema)
            }
        }
    }

    @LocalServerPort
    var port: Int = 0

    protected fun client() = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

    protected fun login(username: String, password: String): String =
        client().post()
            .uri("/auth/login")
            .bodyValue(UserLoginDto(username, password))
            .exchange()
            .expectStatus().isOk
            .expectBody<UserTokenOutputModel>()
            .returnResult()
            .responseBody?.token ?: throw IllegalStateException("Token not found problem with login")

    protected fun adminGetUser(userId: Int, token: String): UserOutputDto? {
        return client().get()
            .uri("/admin/users/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<UserOutputDto>()
            .returnResult()
            .responseBody
    }

    protected fun WebTestClient.ResponseSpec.expectProblem(
        status: HttpStatus,
        expected: Problem,
    ) {
        this.expectStatus().isEqualTo(status)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody<ProblemTests>()
            .value { problem ->
                assertEquals(expected.type, problem?.type)
            }
    }
}