import type { Repository } from "../repository/repository"

export type Team = {
    tid: number,
    name: string,
    description: string | null,
    repos: Repository[],
    members: TeamMember[],
}

export type TeamMember = {
    uid: number,
    name: string,
    email: string,
    teamRole: TeamRole,
}

export type TeamRole = "LEADER" | "COLLABORATOR"

export type SimpleTeam = {
    tid: number,
    name: string,
    description: string | null,
}

export type SimpleTeamsListOutput = {
    teams: SimpleTeam[]
}

export type SimpleTeamWithCount = {
    tid: number,
    name: string,
    description: string | null,
    repoCount: number,
    memberCount: number,
}

export type SimpleTeamWithCountListOutput = {
    teams: SimpleTeamWithCount[]
}

export type CountsBySeverity = {
    critical: number
    high: number
    medium: number
    low: number
    unknown: number
}

export type VulnerabilityStats = {
    open: number
    fixed: number
    dismissed: number
    countsBySeverity: CountsBySeverity
}

export type SastStats = {
    open: number
    fixed: number
    dismissed: number
    countsBySeverity: CountsBySeverity
}

export type RepoProblemCount = {
    rid: number
    vulnerabilities: CountsBySeverity
    sastAlerts: CountsBySeverity
}

export type TeamStats = {
    vulnerabilityStats: VulnerabilityStats
    sastStats: SastStats
    repoCounts: RepoProblemCount[]
}

export type DailySastCount = {
    date: string
    count: number
    countsBySeverity: CountsBySeverity
}

export type DailyVulnerabilityCount = {
    date: string
    count: number
    countsBySeverity: CountsBySeverity
}

export type DailySastCountList = DailySastCount[]

export type DailyVulnerabilityCountList = DailyVulnerabilityCount[]

export type HistoryStats = {
    sastList: DailySastCountList
    vulnList: DailyVulnerabilityCountList
}