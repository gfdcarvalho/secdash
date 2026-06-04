package com.isel.ps.secdash.model.teams

import java.time.Instant

data class TeamVulnerabilityHistory(
    val scanId: Int,
    val rid: Int,
    val scannedAt: Instant,
    val vulnerabilityCount: Int,
)