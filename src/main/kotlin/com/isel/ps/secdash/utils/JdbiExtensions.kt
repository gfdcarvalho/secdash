package com.isel.ps.secdash.utils

import com.isel.ps.secdash.model.users.TokenValidationInfo
import com.isel.ps.secdash.repository.mappers.TokenValidationInfoMapper
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import kotlin.jvm.java

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())

    return this
}