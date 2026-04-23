package com.isel.ps.secdash.controller

import com.isel.ps.secdash.SecdashApplication
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [SecdashApplication::class, TestJdbiConfig::class],
    properties = ["spring.main.allow-bean-definition-overriding=true"],
)
class GithubControllerTests : ControllerTestsBase() {
}