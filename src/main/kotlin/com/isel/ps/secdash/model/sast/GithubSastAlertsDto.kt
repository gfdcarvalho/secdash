package com.isel.ps.secdash.model.sast

import com.fasterxml.jackson.annotation.JsonProperty
import com.isel.ps.secdash.model.Platform
import java.time.Instant

data class GithubSastAlertsDto(
    val number: Int,
    val state: String,
    @JsonProperty("html_url") val htmlUrl: String,
    @JsonProperty("created_at") val createdAt: String,
    @JsonProperty("updated_at") val updatedAt: String,
    @JsonProperty("fixed_at") val fixedAt: String?,
    @JsonProperty("dismissed_at") val dismissedAt: String?,
    val rule: GithubSastRuleDto,
    val tool: GithubSastToolDto,
    @JsonProperty("most_recent_instance") val mostRecentInstance: GithubSastInstanceDto?,
) {
    fun toExternalSastAlerts() = ExternalSastAlerts(
        externalId = number.toString(),
        state = when {
            fixedAt != null -> ExternalSastAlerts.SastAlertState.FIXED
            dismissedAt != null -> ExternalSastAlerts.SastAlertState.DISMISSED
            else -> ExternalSastAlerts.SastAlertState.OPEN
        },
        ruleId = rule.id,
        ruleDescription = rule.description,
        severity = when (rule.securitySeverityLevel?.lowercase()) {
            "critical" -> ExternalSastAlerts.SastSeverity.CRITICAL
            "high" -> ExternalSastAlerts.SastSeverity.HIGH
            "medium" -> ExternalSastAlerts.SastSeverity.MEDIUM
            "low" -> ExternalSastAlerts.SastSeverity.LOW
            else -> ExternalSastAlerts.SastSeverity.UNKNOWN
        },
        toolName = tool.name,
        filePath = mostRecentInstance?.location?.path,
        startLine = mostRecentInstance?.location?.startLine,
        endLine = mostRecentInstance?.location?.endLine,
        message = mostRecentInstance?.message?.text,
        htmlUrl = htmlUrl,
        platform = Platform.GITHUB,
        detectedAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
    )
}

data class GithubSastRuleDto(
    val id: String,
    val name: String,
    val description: String,
    @JsonProperty("security_severity_level") val securitySeverityLevel: String?,
)

data class GithubSastToolDto(
    val name: String,
    val version: String?,
)

data class GithubSastInstanceDto(
    val state: String,
    val message: GithubSastMessageDto?,
    val location: GithubSastLocationDto?,
)

data class GithubSastMessageDto(
    val text: String,
)

data class GithubSastLocationDto(
    val path: String,
    @JsonProperty("start_line") val startLine: Int,
    @JsonProperty("end_line") val endLine: Int,
)