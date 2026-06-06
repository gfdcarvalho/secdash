export type Platform = "GITHUB" | "GITLAB"

export type Visibility = "PUBLIC" | "PRIVATE" | "INTERNAL"

export type Owner = {
    oid: number,
    externalId: string,
    name: string,
    url: string,
    avatarUrl: string | null,
    platform: Platform,
}

export type Repository = {
    rid: number,
    name: string,
    externalId: string,
    platform: Platform,
    owner: Owner,
    htmlUrl: string,
    description: string,
    issuesCount: number,
    createdAt: string,
    updatedAt: string,
    forksCount: number,
    visibility: Visibility,
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

export type RepoStats = {
    vulnerabilityStats: VulnerabilityStats
    sastStats: SastStats
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
