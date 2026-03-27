package com.isel.ps.secdash

import com.isel.ps.secdash.utils.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

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
}

fun main(args: Array<String>) {
	runApplication<SecdashApplication>(*args)
}
