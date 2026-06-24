package com.isel.ps.secdash.controller.utils

import com.isel.ps.secdash.utils.configureWithAppRequirements
import com.isel.ps.secdash.utils.env
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestJdbiConfig {
    @Bean
    @Primary
    fun jdbi(): Jdbi = Jdbi.create(
        PGSimpleDataSource().apply {
            setURL(env("JDBC_DATABASE_TEST_URL"))
        },
    ).configureWithAppRequirements()
}