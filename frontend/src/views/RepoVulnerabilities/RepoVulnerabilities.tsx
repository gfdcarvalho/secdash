import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import type { ExternalVulnerability, RepositoryVulnerabilities, VulnerabilitySeverity } from '../../model/vulnerabilities/vulnerabilities'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import Style from './RepoVulnerabilities.module.css'

const ALL_SEVERITIES: VulnerabilitySeverity[] = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN']
const DATE_OPTIONS = [0, 7, 30, 90] as const
type DateOption = typeof DATE_OPTIONS[number]

const MS_PER_DAY = 24 * 60 * 60 * 1000 // hours × minutes × seconds × ms

const SEVERITY_RANK: Record<VulnerabilitySeverity, number> = { // para podermos ordenar por severidade temos de atribuir um valor a cada uma delas 
    CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3, UNKNOWN: 4,
}
type SortOption = 'none' | 'severity_desc' | 'severity_asc' | 'date_desc' | 'date_asc'

export function RepoVulnerabilities() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const location = useLocation()
    const { repoId, platform } = useParams<{ repoId: string; platform: string }>()

    const [vulnerabilities, setVulnerabilities] = useState<ExternalVulnerability[] | null>(
        location.state?.vulnerabilities?.vulnerabilities ?? null
    )
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')
    const [selectedSeverities, setSelectedSeverities] = useState<Set<VulnerabilitySeverity>>(new Set())
    const [dateFilter, setDateFilter] = useState<DateOption>(0)
    const [sortOption, setSortOption] = useState<SortOption>('none')

    const fetchVulnerabilities = async () => {
        const uri = platform === 'github'
            ? `/github/repositories/${repoId}/dependabot`
            : `/gitlab/repositories/${repoId}/dependency-scanning`
        const response = await api.get<RepositoryVulnerabilities>(uri)
        if (isSuccess(response)) {
            setVulnerabilities(response.value.data.vulnerabilities)
        } else {
            setError(true)
        }
    }

    useEffect(() => {
        if (!vulnerabilities) {
            fetchVulnerabilities()
        }
    }, [repoId, platform])

    const toggleSeverity = (severity: VulnerabilitySeverity) => {
        setSelectedSeverities(currentSev => {
            const next = new Set(currentSev)
            next.has(severity) ? next.delete(severity) : next.add(severity)
            return next
        })
    }

    const filtered = useMemo(() => {
        if (!vulnerabilities) return []
        const searchLower = search.toLowerCase()
        const cutoff = dateFilter > 0 ? Date.now() - dateFilter * MS_PER_DAY : null
        const result = vulnerabilities.filter(vuln => { 
            // filtragem pela barra de pesquisa 
            const matchesSearch = !searchLower
                || vuln.title.toLowerCase().includes(searchLower)
                || vuln.packageName.toLowerCase().includes(searchLower)
            
            // filtragem pela severidade
            const matchesSeverity = selectedSeverities.size === 0
                || selectedSeverities.has(vuln.severity)

            // filtragem pela data 
            const detectedMs = vuln.detectedAt ? new Date(vuln.detectedAt).getTime() : null
            const matchesDate = !cutoff
                || (detectedMs !== null && detectedMs >= cutoff)

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
                        <div key={vuln.externalId} className={Style.vulnCard}>
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
                                <p className={Style.vulnDescription}>{vuln.description}</p>
                            )}

                            <div className={Style.vulnMeta}>
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
