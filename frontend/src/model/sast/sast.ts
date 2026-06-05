export type SastAlertState = 'OPEN' | 'FIXED' | 'DISMISSED'

export type SastSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN'

export interface SastAlert {
  sid: number
  rid: number
  externalId: string
  state: SastAlertState
  ruleId: string
  ruleDescription: string
  severity: SastSeverity
  toolName: string
  filePath: string | null
  startLine: number | null
  endLine: number | null
  message: string | null
  htmlUrl: string
  platform: string
  detectedAt: string | null
  updatedAt: string | null
}

export interface RepositorySast {
  rid: number
  sastAlerts: SastAlert[]
}

export interface TeamSastAlerts {
  name: string
  alerts: SastAlert[]
}

export interface SastAlertDetail {
  sid: number
  rid: number
  externalId: string
  state: SastAlertState
  ruleId: string
  ruleDescription: string
  severity: SastSeverity
  toolName: string
  filePath: string | null
  startLine: number | null
  endLine: number | null
  message: string | null
  htmlUrl: string
  platform: string
  detectedAt: string | null
  updatedAt: string | null
  repoName: string
  repoHtmlUrl: string
  ownerName: string
  ownerAvatarUrl: string | null
}
