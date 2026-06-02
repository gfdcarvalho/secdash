package com.isel.ps.secdash.model.teams

enum class StatState    { OPEN, FIXED, DISMISSED }
enum class StatSeverity { CRITICAL, HIGH, MEDIUM, LOW, UNKNOWN }

data class StatRowSqlDto(
    val state: StatState,
    val severity: StatSeverity,
    val count: Int,
)

data class TeamStats(
    val vulnerabilityStats: VulnerabilityStats,
    val sastStats: SastStats,
)

data class VulnerabilityStats(
    val open: Int,
    val fixed: Int,
    val dismissed: Int,
    val countsBySeverity: CountsBySeverity,
) {
    companion object {
        fun from(rows: List<StatRowSqlDto>): VulnerabilityStats {
            val byState = rows.groupBy { it.state }.mapValues { (_, r) -> r.sumOf { it.count } }
            return VulnerabilityStats(
                open             = byState[StatState.OPEN]      ?: 0,
                fixed            = byState[StatState.FIXED]     ?: 0,
                dismissed        = byState[StatState.DISMISSED] ?: 0,
                countsBySeverity = CountsBySeverity.from(rows.filter { it.state == StatState.OPEN }),
            )
        }
    }
}

data class SastStats(
    val open: Int,
    val fixed: Int,
    val dismissed: Int,
    val countsBySeverity: CountsBySeverity,
) {
    companion object {
        fun from(rows: List<StatRowSqlDto>): SastStats {
            val byState = rows.groupBy { it.state }.mapValues { (_, r) -> r.sumOf { it.count } }
            return SastStats(
                open             = byState[StatState.OPEN]      ?: 0,
                fixed            = byState[StatState.FIXED]     ?: 0,
                dismissed        = byState[StatState.DISMISSED] ?: 0,
                countsBySeverity = CountsBySeverity.from(rows.filter { it.state == StatState.OPEN }),
            )
        }
    }
}

data class CountsBySeverity(
    val critical: Int,
    val high: Int,
    val medium: Int,
    val low: Int,
    val unknown: Int,
) {
    companion object {
        fun from(openRows: List<StatRowSqlDto>): CountsBySeverity {
            val bySeverity = openRows.associate { it.severity to it.count }
            return CountsBySeverity(
                critical = bySeverity[StatSeverity.CRITICAL] ?: 0,
                high     = bySeverity[StatSeverity.HIGH]     ?: 0,
                medium   = bySeverity[StatSeverity.MEDIUM]   ?: 0,
                low      = bySeverity[StatSeverity.LOW]      ?: 0,
                unknown  = bySeverity[StatSeverity.UNKNOWN]  ?: 0,
            )
        }
    }
}