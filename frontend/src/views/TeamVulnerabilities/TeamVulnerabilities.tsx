import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Vulnerability, TeamVulnerabilities, VulnerabilitySeverity } from '../../model/vulnerabilities/vulnerabilities'
import type { Team } from '../../model/teams/teams'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import { buildCsvReport, downloadCsv, sanitizeFilename, currentDateStamp } from '../../utils/exportCsv'
import Style from './TeamVulnerabilities.module.css'
import { useLocation } from 'react-router'
import Markdown from 'react-markdown'

const ALL_SEVERITIES: VulnerabilitySeverity[] = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN']
const DATE_OPTIONS = [0, 7, 30, 90] as const
type DateOption = typeof DATE_OPTIONS[number]

const MS_PER_DAY = 24 * 60 * 60 * 1000

const SEVERITY_RANK: Record<VulnerabilitySeverity, number> = {
    CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3, UNKNOWN: 4,
}
type SortOption = 'none' | 'severity_desc' | 'severity_asc' | 'date_desc' | 'date_asc'

type VulnerabilityWithRepo = Vulnerability & { repoName: string }

export function TeamVulnerabilitiesView() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const location = useLocation()
    const { teamId } = useParams<{ teamId: string }>()

    const [vulnerabilities, setVulnerabilities] = useState<VulnerabilityWithRepo[] | null>(null)
    const [team, setTeam] = useState<Team | null>(null)
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')

    const [selectedSeverities, setSelectedSeverities] =
        useState<Set<VulnerabilitySeverity>>(() => {
            const sev = location.state?.severity as VulnerabilitySeverity | undefined
            return sev ? new Set([sev]) : new Set()
        })

    const [dateFilter, setDateFilter] = useState<DateOption>(0)
    const [sortOption, setSortOption] = useState<SortOption>('none')

    useEffect(() => {
        const sev = location.state?.severity as VulnerabilitySeverity | undefined
        if (sev) {
            setSelectedSeverities(new Set([sev]))
        }
    }, [location.state])

    useEffect(() => {
        const fetch = async () => {
            const response = await api.get<TeamVulnerabilities[]>(`/teams/${teamId}/vulnerabilities`)
            if (isSuccess(response)) {
                const flat = response.value.data.flatMap(tv =>
                    tv.vulnerabilities.map(v => ({ ...v, repoName: tv.name }))
                )
                setVulnerabilities(flat)
            } else {
                setError(true)
            }
        }
        // vai buscar a informação da equipa para incluir no relatório CSV exportado
        const fetchTeam = async () => {
            const response = await api.get<Team>(`/teams/${teamId}`)
            if (isSuccess(response)) {
                setTeam(response.value.data)
            }
        }
        fetch()
        fetchTeam()
    }, [teamId])

    const exportCsv = () => {
        const severityCounts = ALL_SEVERITIES.map(sev =>
            [sev, filtered.filter(vuln => vuln.severity === sev).length] as [string, number]
        )
        const analyzedRepos = team
            ? team.repos.map(repo => repo.name)
            : [...new Set(filtered.map(vuln => vuln.repoName))]
        const content = buildCsvReport({
            title: t.csvExport.vulnReportTitle,
            metadata: [
                [t.csvExport.team, team?.name ?? teamId],
                [t.csvExport.repositories, analyzedRepos.join(', ')],
                [t.csvExport.generatedAt, new Date().toLocaleString()],
            ],
            summaryTitle: t.csvExport.summary,
            summary: [[t.csvExport.total, filtered.length], ...severityCounts],
            headers: [
                t.csvExport.repository, t.csvExport.colTitle, t.csvExport.colSeverity,
                t.csvExport.colState, t.csvExport.colPackage, t.csvExport.colVersion,
                t.csvExport.colFixedVersion, t.csvExport.colCvssScore, t.csvExport.colCve,
                t.csvExport.colManifestPath, t.csvExport.colDetectedAt,
            ],
            rows: filtered.map(vuln => [
                vuln.repoName, vuln.title, vuln.severity,
                vuln.state, vuln.packageName, vuln.packageVersion,
                vuln.fixedVersion, vuln.cvssScore, vuln.cveId,
                vuln.manifestPath,
                vuln.detectedAt ? new Date(vuln.detectedAt).toLocaleDateString() : null,
            ]),
        })
        const name = sanitizeFilename(team?.name ?? `team-${teamId}`)
        downloadCsv(`secdash-team-vulnerabilities-${name}-${currentDateStamp()}.csv`, content)
    }

    const toggleSeverity = (severity: VulnerabilitySeverity) => {
        setSelectedSeverities(prev => {
            const next = new Set(prev)
            next.has(severity) ? next.delete(severity) : next.add(severity)
            return next
        })
    }

    const filtered = useMemo(() => {
        if (!vulnerabilities) return []
        const searchLower = search.toLowerCase()
        const cutoff = dateFilter > 0 ? Date.now() - dateFilter * MS_PER_DAY : null
        const result = vulnerabilities.filter(vuln => {
            const matchesSearch = !searchLower
                || vuln.title.toLowerCase().includes(searchLower)
                || vuln.packageName.toLowerCase().includes(searchLower)
                || vuln.repoName.toLowerCase().includes(searchLower)
            const matchesSeverity = selectedSeverities.size === 0 || selectedSeverities.has(vuln.severity)
            const detectedMs = vuln.detectedAt ? new Date(vuln.detectedAt).getTime() : null
            const matchesDate = !cutoff || (detectedMs !== null && detectedMs >= cutoff)
            return matchesSearch && matchesSeverity && matchesDate
        })
        switch (sortOption) {
            case 'severity_desc': result.sort((a, b) => SEVERITY_RANK[a.severity] - SEVERITY_RANK[b.severity]); break
            case 'severity_asc':  result.sort((a, b) => SEVERITY_RANK[b.severity] - SEVERITY_RANK[a.severity]); break
            case 'date_desc':     result.sort((a, b) => (b.detectedAt ?? '').localeCompare(a.detectedAt ?? '')); break
            case 'date_asc':      result.sort((a, b) => (a.detectedAt ?? '').localeCompare(b.detectedAt ?? '')); break
        }
        return result
    }, [vulnerabilities, search, selectedSeverities, dateFilter, sortOption])

    const dateLabel = (opt: DateOption) => {
        if (opt === 0) return t.repoVulnerabilities.filterDateAll
        if (opt === 7) return t.repoVulnerabilities.filterDate7
        if (opt === 30) return t.repoVulnerabilities.filterDate30
        return t.repoVulnerabilities.filterDate90
    }

    if (error) {
        return (
            <div className={Style.content}>
                <div className={Style.topSection}>
                    <button className={Style.backButton} onClick={() => navigate(-1)}>
                        {t.repoVulnerabilities.backButton}
                    </button>
                </div>
                <p className={Style.message}>{t.repoVulnerabilities.error}</p>
            </div>
        )
    }

    if (!vulnerabilities) {
        return (
            <div className={Style.content}>
                <p className={Style.message}>{t.repoVulnerabilities.loading}</p>
            </div>
        )
    }

    return (
        <div className={Style.content}>
            <div className={Style.topSection}>
                <button className={Style.backButton} onClick={() => navigate(-1)}>
                    {t.repoVulnerabilities.backButton}
                </button>
                <span className={Style.count}>{filtered.length}</span>
                <button className={Style.exportButton} onClick={exportCsv} disabled={filtered.length === 0}>
                    {t.csvExport.button}
                </button>
            </div>

            <div className={Style.filterSection}>
                <input
                    className={Style.searchBar}
                    type="text"
                    placeholder={t.repoVulnerabilities.searchPlaceholder}
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                <div className={Style.filterRow}>
                    <div className={Style.severityChips}>
                        {ALL_SEVERITIES.map(sev => (
                            <button
                                key={sev}
                                className={`${Style.severityChip} ${Style[`chip_${sev.toLowerCase()}`]} ${selectedSeverities.has(sev) ? Style.chipActive : ''}`}
                                onClick={() => toggleSeverity(sev)}
                            >
                                {sev}
                            </button>
                        ))}
                    </div>
                    <div className={Style.selects}>
                        <select
                            className={Style.dateSelect}
                            value={dateFilter}
                            onChange={e => setDateFilter(Number(e.target.value) as DateOption)}
                        >
                            {DATE_OPTIONS.map(opt => (
                                <option key={opt} value={opt}>{dateLabel(opt)}</option>
                            ))}
                        </select>
                        <select
                            className={Style.dateSelect}
                            value={sortOption}
                            onChange={e => setSortOption(e.target.value as SortOption)}
                        >
                            <option value="none">{t.repoVulnerabilities.sortNone}</option>
                            <option value="severity_desc">{t.repoVulnerabilities.sortSeverityDesc}</option>
                            <option value="severity_asc">{t.repoVulnerabilities.sortSeverityAsc}</option>
                            <option value="date_desc">{t.repoVulnerabilities.sortDateDesc}</option>
                            <option value="date_asc">{t.repoVulnerabilities.sortDateAsc}</option>
                        </select>
                    </div>
                </div>
            </div>

            <div className={Style.listSection}>
                {filtered.length === 0 ? (
                    <p className={Style.message}>{t.repoVulnerabilities.noVulnerabilities}</p>
                ) : (
                    filtered.map(vuln => (
                        <div key={`${vuln.repoName}-${vuln.externalId}`} className={Style.vulnCard} onClick={() => navigate(`/vulnerability/${vuln.vid}`)}>
                            <div className={Style.vulnHeader}>
                                <span className={Style.vulnTitle}>{vuln.title}</span>
                                <div className={Style.badges}>
                                    <span className={`${Style.severityBadge} ${Style[`severity_${vuln.severity.toLowerCase()}`]}`}>
                                        {vuln.severity}
                                    </span>
                                    <span className={`${Style.stateBadge} ${Style[`state_${vuln.state.toLowerCase()}`]}`}>
                                        {vuln.state}
                                    </span>
                                </div>
                            </div>

                            {vuln.description && (
                                <div className={Style.vulnDescription}><Markdown>{vuln.description}</Markdown></div>
                            )}

                            <div className={Style.vulnMeta}>
                                <div className={Style.metaItem}>
                                    <span className={Style.metaLabel}>Repository</span>
                                    <span className={Style.metaValue}>{vuln.repoName}</span>
                                </div>
                                <div className={Style.metaItem}>
                                    <span className={Style.metaLabel}>{t.repoVulnerabilities.package}</span>
                                    <span className={Style.metaValue}>{vuln.packageName}</span>
                                </div>
                                {vuln.packageVersion && (
                                    <div className={Style.metaItem}>
                                        <span className={Style.metaLabel}>{t.repoVulnerabilities.version}</span>
                                        <span className={Style.metaValue}>{vuln.packageVersion}</span>
                                    </div>
                                )}
                                {vuln.fixedVersion && (
                                    <div className={Style.metaItem}>
                                        <span className={Style.metaLabel}>{t.repoVulnerabilities.fixedVersion}</span>
                                        <span className={Style.metaValue}>{vuln.fixedVersion}</span>
                                    </div>
                                )}
                                {vuln.cvssScore !== null && (
                                    <div className={Style.metaItem}>
                                        <span className={Style.metaLabel}>{t.repoVulnerabilities.cvssScore}</span>
                                        <span className={Style.metaValue}>{vuln.cvssScore.toFixed(1)}</span>
                                    </div>
                                )}
                                {vuln.cveId && (
                                    <div className={Style.metaItem}>
                                        <span className={Style.metaLabel}>CVE</span>
                                        <span className={Style.metaValue}>{vuln.cveId}</span>
                                    </div>
                                )}
                                {vuln.manifestPath && (
                                    <div className={Style.metaItem}>
                                        <span className={Style.metaLabel}>{t.repoVulnerabilities.manifestPath}</span>
                                        <span className={Style.metaValue}>{vuln.manifestPath}</span>
                                    </div>
                                )}
                                {vuln.detectedAt && (
                                    <div className={Style.metaItem}>
                                        <span className={Style.metaLabel}>{t.repoVulnerabilities.detectedAt}</span>
                                        <span className={Style.metaValue}>{new Date(vuln.detectedAt).toLocaleDateString()}</span>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    )
}
