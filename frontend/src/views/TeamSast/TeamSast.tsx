import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import type { SastAlert, TeamSastAlerts, SastSeverity } from '../../model/sast/sast'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import Style from './TeamSast.module.css'

const ALL_SEVERITIES: SastSeverity[] = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN']
const DATE_OPTIONS = [0, 7, 30, 90] as const
type DateOption = typeof DATE_OPTIONS[number]

const MS_PER_DAY = 24 * 60 * 60 * 1000

const SEVERITY_RANK: Record<SastSeverity, number> = {
    CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3, UNKNOWN: 4,
}
type SortOption = 'none' | 'severity_desc' | 'severity_asc' | 'date_desc' | 'date_asc'

type AlertWithRepo = SastAlert & { repoName: string }

export function TeamSast() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const { teamId } = useParams<{ teamId: string }>()

    const [alerts, setAlerts] = useState<AlertWithRepo[] | null>(null)
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')
    const [selectedSeverities, setSelectedSeverities] = useState<Set<SastSeverity>>(new Set())
    const [dateFilter, setDateFilter] = useState<DateOption>(0)
    const [sortOption, setSortOption] = useState<SortOption>('none')

    useEffect(() => {
        const fetch = async () => {
            const response = await api.get<TeamSastAlerts[]>(`/teams/${teamId}/sast`)
            if (isSuccess(response)) {
                const flat = response.value.data.flatMap(ta =>
                    ta.alerts.map(a => ({ ...a, repoName: ta.name }))
                )
                setAlerts(flat)
            } else {
                setError(true)
            }
        }
        fetch()
    }, [teamId])

    const toggleSeverity = (severity: SastSeverity) => {
        setSelectedSeverities(prev => {
            const next = new Set(prev)
            next.has(severity) ? next.delete(severity) : next.add(severity)
            return next
        })
    }

    const fileLocation = (alert: SastAlert) => {
        if (!alert.filePath) return null
        if (alert.startLine) {
            return alert.endLine && alert.endLine !== alert.startLine
                ? `${alert.filePath}:${alert.startLine}–${alert.endLine}`
                : `${alert.filePath}:${alert.startLine}`
        }
        return alert.filePath
    }

    const filtered = useMemo(() => {
        if (!alerts) return []
        const searchLower = search.toLowerCase()
        const cutoff = dateFilter > 0 ? Date.now() - dateFilter * MS_PER_DAY : null
        const result = alerts.filter(alert => {
            const matchesSearch = !searchLower
                || alert.ruleDescription.toLowerCase().includes(searchLower)
                || alert.ruleId.toLowerCase().includes(searchLower)
                || alert.toolName.toLowerCase().includes(searchLower)
                || (alert.filePath?.toLowerCase().includes(searchLower) ?? false)
                || alert.repoName.toLowerCase().includes(searchLower)
            const matchesSeverity = selectedSeverities.size === 0 || selectedSeverities.has(alert.severity)
            const detectedMs = alert.detectedAt ? new Date(alert.detectedAt).getTime() : null
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
    }, [alerts, search, selectedSeverities, dateFilter, sortOption])

    const dateLabel = (opt: DateOption) => {
        if (opt === 0) return t.repoSast.filterDateAll
        if (opt === 7) return t.repoSast.filterDate7
        if (opt === 30) return t.repoSast.filterDate30
        return t.repoSast.filterDate90
    }

    if (error) {
        return (
            <div className={Style.content}>
                <div className={Style.topSection}>
                    <button className={Style.backButton} onClick={() => navigate(-1)}>
                        {t.repoSast.backButton}
                    </button>
                </div>
                <p className={Style.message}>{t.repoSast.error}</p>
            </div>
        )
    }

    if (!alerts) {
        return (
            <div className={Style.content}>
                <p className={Style.message}>{t.repoSast.loading}</p>
            </div>
        )
    }

    return (
        <div className={Style.content}>
            <div className={Style.topSection}>
                <button className={Style.backButton} onClick={() => navigate(-1)}>
                    {t.repoSast.backButton}
                </button>
                <span className={Style.count}>{filtered.length}</span>
            </div>

            <div className={Style.filterSection}>
                <input
                    className={Style.searchBar}
                    type="text"
                    placeholder={t.repoSast.searchPlaceholder}
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
                            <option value="none">{t.repoSast.sortNone}</option>
                            <option value="severity_desc">{t.repoSast.sortSeverityDesc}</option>
                            <option value="severity_asc">{t.repoSast.sortSeverityAsc}</option>
                            <option value="date_desc">{t.repoSast.sortDateDesc}</option>
                            <option value="date_asc">{t.repoSast.sortDateAsc}</option>
                        </select>
                    </div>
                </div>
            </div>

            <div className={Style.listSection}>
                {filtered.length === 0 ? (
                    <p className={Style.message}>{t.repoSast.noAlerts}</p>
                ) : (
                    filtered.map(alert => (
                        <div key={`${alert.repoName}-${alert.externalId}`} className={Style.alertCard} onClick={() => navigate(`/sast/${alert.sid}`)}>
                            <div className={Style.alertHeader}>
                                <span className={Style.alertTitle}>{alert.ruleDescription}</span>
                                <div className={Style.badges}>
                                    <span className={`${Style.severityBadge} ${Style[`severity_${alert.severity.toLowerCase()}`]}`}>
                                        {alert.severity}
                                    </span>
                                    <span className={`${Style.stateBadge} ${Style[`state_${alert.state.toLowerCase()}`]}`}>
                                        {alert.state}
                                    </span>
                                </div>
                            </div>

                            {alert.message && (
                                <p className={Style.alertMessage}>{alert.message}</p>
                            )}

                            <div className={Style.alertMeta}>
                                <div className={Style.metaItem}>
                                    <span className={Style.metaLabel}>Repository</span>
                                    <span className={Style.metaValue}>{alert.repoName}</span>
                                </div>
                                <div className={Style.metaItem}>
                                    <span className={Style.metaLabel}>{t.repoSast.rule}</span>
                                    <span className={Style.metaValue}>{alert.ruleId}</span>
                                </div>
                                <div className={Style.metaItem}>
                                    <span className={Style.metaLabel}>{t.repoSast.tool}</span>
                                    <span className={Style.metaValue}>{alert.toolName}</span>
                                </div>
                                {fileLocation(alert) && (
                                    <div className={Style.metaItem}>
                                        <span className={Style.metaLabel}>{t.repoSast.file}</span>
                                        <span className={Style.metaValue}>{fileLocation(alert)}</span>
                                    </div>
                                )}
                                {alert.detectedAt && (
                                    <div className={Style.metaItem}>
                                        <span className={Style.metaLabel}>{t.repoSast.detectedAt}</span>
                                        <span className={Style.metaValue}>{new Date(alert.detectedAt).toLocaleDateString()}</span>
                                    </div>
                                )}
                                <div className={Style.metaItem}>
                                    <span className={Style.metaLabel}>{t.repoSast.viewAlert}</span>
                                    <a className={Style.alertLink} href={alert.htmlUrl} target="_blank" rel="noreferrer">
                                        {t.repoSast.openLink}
                                    </a>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    )
}
