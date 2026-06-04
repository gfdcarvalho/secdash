package com.isel.ps.secdash.model.sast

import com.isel.ps.secdash.model.Platform
import java.time.Instant

data class SastAlertWithRepoSqlDto(
    val sid: Int,
    val rid: Int,
    val repoName: String,
    val externalId: String,
    val state: SastAlert.SastAlertState,
    val ruleId: String,
    val ruleDescription: String,
    val severity: SastAlert.SastSeverity,
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
    fun toSastAlert() = SastAlert(
        sid             = sid,
        rid             = rid,
        externalId      = externalId,
        state           = state,
        ruleId          = ruleId,
        ruleDescription = ruleDescription,
        severity        = severity,
        toolName        = toolName,
        filePath        = filePath,
        startLine       = startLine,
        endLine         = endLine,
        message         = message,
        htmlUrl         = htmlUrl,
        platform        = platform,
        detectedAt      = detectedAt,
        updatedAt       = updatedAt,
    )
}