package com.isel.ps.secdash

import com.isel.ps.secdash.model.users.Sha256TokenEncoder
import com.isel.ps.secdash.model.users.UsersDomainConfig
import com.isel.ps.secdash.utils.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import kotlin.time.Duration.Companion.hours


private fun env(key: String): String { // don't like this but the .env file was not working properly
	val envFile = java.io.File(".env")
	if (envFile.exists()) {
		val line = envFile.readLines().firstOrNull { it.startsWith("$key=") }
		if (line != null) return line.substringAfter("=")
	}
	return System.getenv(key) ?: error("Missing environment variable: $key")
}

@SpringBootApplication
class SecdashApplication{
	@Bean
	fun jdbi(): Jdbi =
		Jdbi.create(
			PGSimpleDataSource().apply {
				setURL(env("JDBC_DATABASE_URL"))
			},
		).configureWithAppRequirements()


	@Bean
	fun passwordEncoder() = BCryptPasswordEncoder()

	@Bean
	fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
	fun clock() = Clock.System

	@Bean
	fun usersDomainConfig() =
		UsersDomainConfig(
			tokenSizeInBytes = 256 / 8,
			tokenTtl = 24.hours,
			tokenRollingTtl = 1.hours,
			maxTokensPerUser = 3,
		)
}

fun main(args: Array<String>) {
	runApplication<SecdashApplication>(*args)
}
