package com.isel.ps.secdash.model.sast

data class TeamSastAlerts(
    val name: String,
    val alerts: List<SastAlert>,
)