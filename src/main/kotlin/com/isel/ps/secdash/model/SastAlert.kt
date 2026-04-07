package com.isel.ps.secdash.model

import com.isel.ps.secdash.model.repositories.Repository

class SastAlert(
    val sid: Int,
    val repository: Repository,
    val state: SastState,
    val severity: SastSeverity,
    val scaner: String,
    val file: String,
    val line: String,
) {
    enum class SastSeverity { CRITICAL, HIGH, MEDIUM, LOW}
    enum class SastState { OPEN, FIXED, DISMISSED }
}