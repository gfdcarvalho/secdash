package com.isel.ps.secdash.model.sast

import com.fasterxml.jackson.annotation.JsonProperty
import com.isel.ps.secdash.model.Platform
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

data class GitlabSastAlertsDto(
    val version: String,
    val vulnerabilities: List<GitlabSastVulnerabilityDto>,
    val scan: GitlabSastScanDto,
) {
    fun toExternalSastAlerts(): List<ExternalSastAlerts> =
        vulnerabilities.map { it.toExternalSastAlerts() }
}

data class GitlabSastVulnerabilityDto(
    val id: String,
    val name: String,
    val description: String,
    val cve: String,
    val severity: String,
    val scanner: GitlabSastScannerDto,
    val location: GitlabSastLocationDto,
    val identifiers: List<GitlabSastIdentifierDto>,
) {
    fun toExternalSastAlerts(
        detectedAt: Instant? = null
    ) = ExternalSastAlerts(
        externalId = id,
        state = SastAlert.SastAlertState.OPEN,
        ruleId = cve,
        ruleDescription = name,
        severity = when (severity.lowercase()) {
            "critical" -> SastAlert.SastSeverity.CRITICAL
            "high" -> SastAlert.SastSeverity.HIGH
            "medium" -> SastAlert.SastSeverity.MEDIUM
            "low" -> SastAlert.SastSeverity.LOW
            else -> SastAlert.SastSeverity.UNKNOWN
        },
        toolName = scanner.name,
        filePath = location.file,
        startLine = location.startLine,
        endLine = null,
        message = description,
        htmlUrl = identifiers.firstOrNull { it.url != null }?.url ?: "",
        platform = Platform.GITLAB,
        detectedAt = detectedAt,
        updatedAt = detectedAt,
    )
}

data class GitlabSastScannerDto(
    val id: String,
    val name: String,
)

data class GitlabSastLocationDto(
    val file: String,
    @JsonProperty("start_line") val startLine: Int?,
)

data class GitlabSastIdentifierDto(
    val type: String,
    val name: String,
    val value: String,
    val url: String? = null,
)

data class GitlabSastScanDto(
    @JsonProperty("start_time") val startTime: String,
    @JsonProperty("end_time") val endTime: String,
    val status: String,
)