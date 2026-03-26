package com.isel.ps.secdash

import com.isel.ps.secdash.utils.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.postgresql.ds.PGSimpleDataSource


@SpringBootApplication
class SecdashApplication{
	@Bean
	fun jdbi(): Jdbi =
		Jdbi.create(
			PGSimpleDataSource().apply {
				setURL(System.getenv("JDBC_DATABASE_URL"))
			},
		).configureWithAppRequirements()
}

fun main(args: Array<String>) {
	runApplication<SecdashApplication>(*args)
}
