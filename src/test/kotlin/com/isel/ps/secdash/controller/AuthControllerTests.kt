package com.isel.ps.secdash.controller

import org.springframework.boot.test.context.SpringBootTest
import com.isel.ps.secdash.SecdashApplication
import com.isel.ps.secdash.controller.utils.ControllerTestsBase
import com.isel.ps.secdash.controller.utils.TestJdbiConfig
import com.isel.ps.secdash.model.users.UserLoginDto
import com.isel.ps.secdash.model.users.UserTokenOutputModel
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [SecdashApplication::class, TestJdbiConfig::class],
    properties = ["spring.main.allow-bean-definition-overriding=true"],
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTests : ControllerTestsBase() {

    @BeforeEach
    fun setup() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
            handle.execute("select test_data_for_AuthControllerTests()")
        }
    }

    @AfterAll
    fun cleanUp() {
        jdbi.useHandle<Exception> { handle ->
            handle.execute("select init()")
        }
    }

    @Test
    fun `login should return 200 and token`() {
        client().post()
            .uri("/auth/login")
            .bodyValue(
                UserLoginDto(
                    username = "testUsername1",
                    password = "testpassword1"
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Set-Cookie")
            .expectBody<UserTokenOutputModel>()
    }

    @Test
    fun `login with wrong credentials should return 400`() {
        client().post()
            .uri("/auth/login")
            .bodyValue(
                UserLoginDto(
                    username = "testUsername1",
                    password = "wrongPassword"
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
    }

    @Test
    fun `login with blank username should return 400`() {
        client().post()
            .uri("/auth/login")
            .bodyValue(
                UserLoginDto(
                    username = " ",
                    password = "testpassword1"
                )
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `login should create token in database`() {
        client().post()
            .uri("/auth/login")
            .bodyValue(UserLoginDto("testUsername1", "testpassword1"))
            .exchange()
            .expectStatus().isOk

        jdbi.useHandle<Exception> { handle ->
            val count = handle.createQuery("select count(*) from tokens")
                .mapTo(Int::class.java)
                .one()

            assert(count == 1)
        }
    }

    @Test
    fun `login with non existing user should return 400`() {
        client().post()
            .uri("/auth/login")
            .bodyValue(UserLoginDto("InexistentUser", "testpassword"))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `logout with logged in user should return 200`() {
        val token = login("testUsername1", "testpassword1")

        client().post()
            .uri("/auth/logout")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Set-Cookie")
    }

    @Test
    fun `logout with user that is not logged in should return 401`() {
        val expiredToken = "cERGsgMlNTMOV4-gHdMf9nUoAoEAecPBhZ1IY_PayLk="

        client().post()
            .uri("/auth/logout")
            .header("Authorization", "Bearer $expiredToken")
            .exchange()
            .expectStatus().isUnauthorized
    }

}