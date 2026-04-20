package com.isel.ps.secdash.model.sast

import com.isel.ps.secdash.model.Platform
import java.time.Instant

data class ExternalSastAlerts(
    val externalId: String,
    val state: SastAlertState,
    val ruleId: String,
    val ruleDescription: String,
    val severity: SastSeverity,
    val toolName: String,
    val filePath: String?,
    val startLine: Int?,
    val endLine: Int?,
    val message: String?,
    val htmlUrl: String,
    val platform: Platform,
    val detectedAt: Instant?,
    val updatedAt: Instant?,
) {
    enum class SastAlertState { OPEN, FIXED, DISMISSED }
    enum class SastSeverity { CRITICAL, HIGH, MEDIUM, LOW, UNKNOWN }
}