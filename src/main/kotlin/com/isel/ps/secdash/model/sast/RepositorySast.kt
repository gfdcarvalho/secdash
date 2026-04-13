package com.isel.ps.secdash.model.sast

data class RepositorySast(
    val rid: Int,
    val sastAlerts: List<ExternalSastAlerts>,
)
