package com.isel.ps.secdash.model.teams

import java.time.Instant
import java.time.LocalDate

data class TeamVulnerabilityHistory(
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
    val countsBySeverity: CountsBySeverity
)

data class TeamSastHistory(
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