package com.isel.ps.secdash.controller

import com.isel.ps.secdash.utils.configureWithAppRequirements
import com.isel.ps.secdash.utils.env
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient

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
}