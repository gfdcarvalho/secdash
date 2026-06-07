package com.isel.ps.secdash.model.repositories

import java.time.Instant
import java.time.LocalDate

data class RepositoryVulnerabilityHistory(
    val scanId: Int,
    val rid: Int,
    val scannedAt: Instant,
    val vulnerabilityCount: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val unknownCount: Int,
)

data class DailyVulnerabilityCount(
    val date: LocalDate,
    val count: Int,
    val countsBySeverity: CountsBySeverity,
)

data class RepositorySastHistory(
    val scanId: Int,
    val rid: Int,
    val scannedAt: Instant,
    val alertCount: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val unknownCount: Int,
)

data class DailySastCount(
    val date: LocalDate,
    val count: Int,
    val countsBySeverity: CountsBySeverity
)