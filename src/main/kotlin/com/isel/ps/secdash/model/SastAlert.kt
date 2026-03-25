package com.isel.ps.secdash.model

class SastAlert(
    val id: Int,
    val state: SastState,
    val severity: SastSeverity,
    val scaner: String,
    val file: String,
    val line: String,
) {
    enum class SastSeverity { CRITICAL, HIGH, MEDIUM, LOW}
    enum class SastState { OPEN, FIXED, DISMISSED }
}