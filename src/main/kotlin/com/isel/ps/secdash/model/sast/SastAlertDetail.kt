package com.isel.ps.secdash.model.sast

import com.isel.ps.secdash.model.Platform
import java.time.Instant

data class SastAlertDetail(
    val sid: Int,
    val rid: Int,
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
    val repoName: String,
    val repoHtmlUrl: String,
    val ownerName: String,
    val ownerAvatarUrl: String?,
)