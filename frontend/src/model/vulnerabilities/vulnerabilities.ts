export type VulnerabilitySeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN'

export type VulnerabilityState = 'OPEN' | 'FIXED' | 'DISMISSED'

export interface Vulnerability {
  vid: number
  rid: number
  externalId: string
  title: string
  description: string | null
  severity: VulnerabilitySeverity
  state: VulnerabilityState
  cveId: string | null
  ghsaId: string | null
  packageName: string
  packageVersion: string | null
  vulnerableVersionRange: string | null
  fixedVersion: string | null
  manifestPath: string | null
  cvssScore: number | null
  cvssVector: string | null
  references: string[]
  platform: string
  detectedAt: string
  updatedAt: string
}

export interface RepositoryVulnerabilities {
  rid: number
  vulnerabilities: Vulnerability[]
}

export interface TeamVulnerabilities {
  name: string
  vulnerabilities: Vulnerability[]
}

export interface VulnerabilityDetail {
  vid: number
  rid: number
  externalId: string
  title: string
  description: string | null
  severity: VulnerabilitySeverity
  state: VulnerabilityState
  cveId: string | null
  ghsaId: string | null
  packageName: string
  packageVersion: string | null
  vulnerableVersionRange: string | null
  fixedVersion: string | null
  manifestPath: string | null
  cvssScore: number | null
  cvssVector: string | null
  references: string[]
  platform: string
  detectedAt: string
  updatedAt: string
  repoName: string
  repoHtmlUrl: string
  ownerName: string
  ownerAvatarUrl: string | null
}
