import { useState } from 'react'
import { PieChart, Pie, Cell, Tooltip, LineChart, Line, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from 'recharts'
import type { CountsBySeverity, HistoryStats, TeamStats } from '../../model/teams/teams'
import Style from './TeamStatsSection.module.css'

const SEVERITY_COLORS = {
    critical: '#e05555',
    high:     '#e07a35',
    medium:   '#e0b535',
    low:      '#5ba85a',
    unknown:  '#888888',
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
    stats: TeamStats,
    historyStats?: HistoryStats
}

function severityData(counts: CountsBySeverity) {
    return ([
        { name: 'Critical', value: counts.critical, color: SEVERITY_COLORS.critical },
        { name: 'High',     value: counts.high,     color: SEVERITY_COLORS.high },
        { name: 'Medium',   value: counts.medium,   color: SEVERITY_COLORS.medium },
        { name: 'Low',      value: counts.low,      color: SEVERITY_COLORS.low },
        { name: 'Unknown',  value: counts.unknown,  color: SEVERITY_COLORS.unknown },
    ]).filter(d => d.value > 0) // este filtro tira os valores com 0 para o grafico não desenhar fatias que não devia
}

function SeverityDonut({ counts, total }: { counts: CountsBySeverity; total: number }) {
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
                            <Cell key={i} fill={entry.color} />
                        ))}
                    </Pie>
                )}
                <Tooltip {...TOOLTIP_STYLE} />
            </PieChart>
            <span className={Style.donutTotal}>{total}</span>
        </div>
    )
}

function StatCard({ title, open, fixed, dismissed, counts }: {
    title: string
    open: number
    fixed: number
    dismissed: number
    counts: CountsBySeverity
}) {
    const severityEntries = [
        { label: 'Critical', value: counts.critical, color: SEVERITY_COLORS.critical },
        { label: 'High',     value: counts.high,     color: SEVERITY_COLORS.high },
        { label: 'Medium',   value: counts.medium,   color: SEVERITY_COLORS.medium },
        { label: 'Low',      value: counts.low,      color: SEVERITY_COLORS.low },
    ]

    return (
        <div className={Style.card}>
            <div className={Style.cardLeft}>
                <span className={Style.cardTitle}>{title}</span>
                <div className={Style.cardBadges}>
                    <span className={Style.badge} data-variant="open">{open} open</span>
                    <span className={Style.badge}>{fixed} fixed</span>
                    <span className={Style.badge}>{dismissed} dismissed</span>
                </div>
                <div className={Style.severityList}>
                    {severityEntries.map(e => (
                        <div key={e.label} className={Style.severityRow}>
                            <span className={Style.severityDot} style={{ background: e.color }} />
                            <span className={Style.severityLabel}>{e.label}</span>
                            <span className={Style.severityValue}>{e.value}</span>
                        </div>
                    ))}
                </div>
            </div>
            <SeverityDonut counts={counts} total={open} />
        </div>
    )
}

const SEVERITY_LINES = [
    { key: 'count',    label: 'Total',   color: '#a0a0a0' },
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
    const [active, setActive] = useState<Set<string>>(
        new Set(SEVERITY_LINES.map(s => s.key))
    )

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
                        <XAxis
                            dataKey="date"
                            tick={{ fontSize: '0.7em', fill: 'var(--color-text)' }}
                            tickLine={false}
                            tickFormatter={d => d.slice(5)}
                        />
                        <YAxis
                            tick={{ fontSize: '0.7em', fill: 'var(--color-text)' }}
                            tickLine={false}
                            axisLine={false}
                            allowDecimals={false}
                        />
                        <Tooltip {...TOOLTIP_STYLE} />
                        {SEVERITY_LINES.map(s => (
                            <Line
                                key={s.key}
                                type="monotone"
                                dataKey={s.key}
                                stroke={s.color}
                                strokeWidth={2}
                                dot={{ r: 3, fill: s.color }}
                                hide={!active.has(s.key)}
                            />
                        ))}
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    )
}

export function TeamStatsSection({ stats, historyStats }: Props) {
    const [view, setView] = useState<'pie' | 'line'>('pie')

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
                    <StatCard
                        title="Vulnerabilities"
                        open={stats.vulnerabilityStats.open}
                        fixed={stats.vulnerabilityStats.fixed}
                        dismissed={stats.vulnerabilityStats.dismissed}
                        counts={stats.vulnerabilityStats.countsBySeverity}
                    />
                    <StatCard
                        title="SAST Alerts"
                        open={stats.sastStats.open}
                        fixed={stats.sastStats.fixed}
                        dismissed={stats.sastStats.dismissed}
                        counts={stats.sastStats.countsBySeverity}
                    />
                </div>
            )}

            {view === 'line' && (
                <div className={Style.grid}>
                    {historyStats ? (
                        <>
                            <HistoryLineChart
                                title="Vulnerabilities"
                                data={historyStats.vulnList}
                            />
                            <HistoryLineChart
                                title="SAST Alerts"
                                data={historyStats.sastList}
                            />
                        </>
                    ) : (
                        <p className={Style.loadingMessage}>Loading history...</p>
                    )}
                </div>
            )}
        </div>
    )
}
