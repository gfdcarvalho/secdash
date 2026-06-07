import { PieChart, Pie, Cell, Tooltip, LineChart, Line, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from 'recharts'
import { useNavigate, useParams } from 'react-router'
import type { CountsBySeverity } from '../../model/teams/teams'
import type {HistoryStats, RepoStats} from '../../model/repository/repository'
import Style from './RepoStatsSection.module.css'
import type {RepositoryVulnerabilities} from "../../model/vulnerabilities/vulnerabilities.ts";
import {api} from "../../utils/fetchApi.ts";
import {isSuccess} from "../../utils/Either.ts";
import type {RepositorySast} from "../../model/sast/sast.ts";
import { useState } from 'react'


const SEVERITY_COLORS = {
    critical: '#e05555',
    high: '#e07a35',
    medium: '#e0b535',
    low: '#5ba85a',
    unknown: '#888888',
}

const SEVERITY_LINES = [
    { key: 'count',    label: 'Total',    color: '#a0a0a0' },
    { key: 'critical', label: 'Critical', color: SEVERITY_COLORS.critical },
    { key: 'high',     label: 'High',     color: SEVERITY_COLORS.high },
    { key: 'medium',   label: 'Medium',   color: SEVERITY_COLORS.medium },
    { key: 'low',      label: 'Low',      color: SEVERITY_COLORS.low },
    { key: 'unknown',  label: 'Unknown',  color: SEVERITY_COLORS.unknown },
]

function HistoryLineChart({ title, data }: {
    title: string
    data: Array<{ date: string; count: number; countsBySeverity: CountsBySeverity }>
}) {
    const [active, setActive] = useState<Set<string>>(new Set(SEVERITY_LINES.map(s => s.key)))
    const flatData = data.map(d => ({ date: d.date, count: d.count, ...d.countsBySeverity }))
    const toggle = (key: string) =>
        setActive(prev => {
            const next = new Set(prev)
            next.has(key) ? next.delete(key) : next.add(key)
            return next
        })

    return (
        <div className={Style.card}>
            <div className={Style.lineChartInner}>
                <div className={Style.lineChartHeader}>
                    <span className={Style.cardTitle}>{title}</span>
                    <div className={Style.severityToggleBar}>
                        {SEVERITY_LINES.map(s => (
                            <button
                                key={s.key}
                                className={Style.severityToggleBtn}
                                data-active={active.has(s.key)}
                                style={{ '--severity-color': s.color } as React.CSSProperties}
                                onClick={() => toggle(s.key)}
                            >
                                {s.label}
                            </button>
                        ))}
                    </div>
                </div>
                <ResponsiveContainer width="100%" height={160}>
                    <LineChart data={flatData} margin={{ top: 8, right: 8, left: -20, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(128,128,128,0.15)" />
                        <XAxis dataKey="date" tick={{ fontSize: '0.7em', fill: 'var(--color-text)' }} tickLine={false} tickFormatter={d => d.slice(5)} />
                        <YAxis tick={{ fontSize: '0.7em', fill: 'var(--color-text)' }} tickLine={false} axisLine={false} allowDecimals={false} />
                        <Tooltip {...TOOLTIP_STYLE} />
                        {SEVERITY_LINES.map(s => (
                            <Line key={s.key} type="monotone" dataKey={s.key} stroke={s.color} strokeWidth={2} dot={{ r: 3, fill: s.color }} hide={!active.has(s.key)} />
                        ))}
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    )
}


const TOOLTIP_STYLE = {
    contentStyle: {
        backgroundColor: 'var(--color-bg)',
        border: '1px solid rgba(128,128,128,0.3)',
        borderRadius: '0.4em',
        color: 'var(--color-text)',
        fontSize: '0.8em',
        padding: '0.4em 0.7em',
    },
    itemStyle: { color: 'var(--color-text)' },
}

type Props = {
    stats: RepoStats
    platform: string
    rid: number
    historyStats?: HistoryStats
}

function severityData(counts: CountsBySeverity) {
    return [
        { name: 'Critical', value: counts.critical, color: SEVERITY_COLORS.critical },
        { name: 'High', value: counts.high, color: SEVERITY_COLORS.high },
        { name: 'Medium', value: counts.medium, color: SEVERITY_COLORS.medium },
        { name: 'Low', value: counts.low, color: SEVERITY_COLORS.low },
        { name: 'Unknown', value: counts.unknown, color: SEVERITY_COLORS.unknown },
    ].filter(d => d.value > 0)
}

function SeverityDonut({
                           counts,
                           total,
                           onClickSeverity,
                       }: {
    counts: CountsBySeverity
    total: number
    onClickSeverity?: (severity: string) => void
}) {
    const data = severityData(counts)
    const isEmpty = data.length === 0

    return (
        <div className={Style.donutWrapper}>
            <PieChart width={160} height={160}>
                {isEmpty ? (
                    <Pie
                        data={[{ value: 1 }]}
                        cx={75}
                        cy={75}
                        innerRadius={50}
                        outerRadius={70}
                        dataKey="value"
                        stroke="none"
                    >
                        <Cell fill="rgba(128,128,128,0.15)" />
                    </Pie>
                ) : (
                    <Pie
                        data={data}
                        cx={75}
                        cy={75}
                        innerRadius={50}
                        outerRadius={70}
                        dataKey="value"
                        stroke="none"
                    >
                        {data.map((entry, i) => (
                            <Cell
                                key={i}
                                fill={entry.color}
                                onClick={(e) => {
                                    e.stopPropagation()
                                    onClickSeverity?.(entry.name.toUpperCase())
                                }}
                                style={{ cursor: 'pointer' }}
                            />
                        ))}
                    </Pie>
                )}
                <Tooltip {...TOOLTIP_STYLE} />
            </PieChart>
            <span className={Style.donutTotal}>{total}</span>
        </div>
    )
}

function StatCard({
                      title,
                      open,
                      fixed,
                      dismissed,
                      counts,
                      onClickSeverity,
                  }: {
    title: string
    open: number
    fixed: number
    dismissed: number
    counts: CountsBySeverity
    onClickSeverity?: (severity: string) => void
}) {
    const severityEntries = [
        { label: 'Critical', value: counts.critical, color: SEVERITY_COLORS.critical },
        { label: 'High', value: counts.high, color: SEVERITY_COLORS.high },
        { label: 'Medium', value: counts.medium, color: SEVERITY_COLORS.medium },
        { label: 'Low', value: counts.low, color: SEVERITY_COLORS.low },
    ]

    return (
        <div className={Style.card}>
            <div className={Style.cardLeft}>
                <span className={Style.cardTitle}>{title}</span>

                <div className={Style.cardBadges}>
                    <span className={Style.badge} data-variant="open">
                        {open} open
                    </span>
                    <span className={Style.badge}>{fixed} fixed</span>
                    <span className={Style.badge}>{dismissed} dismissed</span>
                </div>

                <div className={Style.severityList}>
                    {severityEntries.map(e => (
                        <div key={e.label} className={Style.severityRow}>
                            <span
                                className={Style.severityDot}
                                style={{ background: e.color }}
                            />
                            <span className={Style.severityLabel}>{e.label}</span>
                            <span className={Style.severityValue}>{e.value}</span>
                        </div>
                    ))}
                </div>
            </div>

            <SeverityDonut
                counts={counts}
                total={open}
                onClickSeverity={onClickSeverity}
            />
        </div>
    )
}

export function RepoStatsSection({ stats, platform, rid, historyStats }: Props) {
    const [view, setView] = useState<'pie' | 'line'>('pie')
    const navigate = useNavigate()
    const { repoId } = useParams<{ repoId: string }>()

    const handleVulnClick = async (severity: string) => {
        const uri = platform === 'GITHUB'
            ? `/github/repositories/${rid}/dependabot`
            : `/gitlab/repositories/${rid}/dependency-scanning`
        const response = await api.get<RepositoryVulnerabilities>(uri)
        if (isSuccess(response)) {
            navigate(`/repos/${repoId}/vulnerabilities/${platform.toLowerCase()}`, {
                state: { vulnerabilities: response.value, severity }
            })
        }
    }

    const handleSastClick = async (severity: string) => {
        const uri = platform === 'GITHUB'
            ? `/github/repositories/${rid}/sast`
            : `/gitlab/repositories/${rid}/sast`
        const response = await api.get<RepositorySast>(uri)
        if (isSuccess(response)) {
            navigate(`/repos/${repoId}/sast/${platform.toLowerCase()}`, {
                state: { sastAlerts: response.value, severity }
            })
        }
    }

    return (
        <div className={Style.statsSection}>
            <div className={Style.toggleBar}>
                <button className={Style.toggleBtn} data-active={view === 'pie'} onClick={() => setView('pie')}>
                    Overview
                </button>
                <button className={Style.toggleBtn} data-active={view === 'line'} onClick={() => setView('line')}>
                    History
                </button>
            </div>

            {view === 'pie' && (
                <div className={Style.grid}>
                    <StatCard title="Vulnerabilities" open={stats.vulnerabilityStats.open} fixed={stats.vulnerabilityStats.fixed} dismissed={stats.vulnerabilityStats.dismissed} counts={stats.vulnerabilityStats.countsBySeverity} onClickSeverity={handleVulnClick} />
                    <StatCard title="SAST Alerts" open={stats.sastStats.open} fixed={stats.sastStats.fixed} dismissed={stats.sastStats.dismissed} counts={stats.sastStats.countsBySeverity} onClickSeverity={handleSastClick} />
                </div>
            )}

            {view === 'line' && (
                <div className={Style.grid}>
                    {historyStats ? (
                        <>
                            <HistoryLineChart title="Vulnerabilities" data={historyStats.vulnList} />
                            <HistoryLineChart title="SAST Alerts" data={historyStats.sastList} />
                        </>
                    ) : (
                        <p className={Style.loadingMessage}>Loading history...</p>
                    )}
                </div>
            )}
        </div>
    )
}
