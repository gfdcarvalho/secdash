import { useState } from 'react'
import { PieChart, Pie, Cell, Tooltip, LineChart, Line, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from 'recharts'
import type { CountsBySeverity } from '../model/teams/teams'
import { useTranslation } from '../i18n/I18nProvider'
import { SEVERITY_COLORS, TOOLTIP_STYLE } from '../utils/statsConstants'
import Style from './StatsComponents.module.css'

export function severityData(counts: CountsBySeverity, labels: { critical: string; high: string; medium: string; low: string; unknown: string }) {
    return [
        { name: labels.critical, key: 'CRITICAL', value: counts.critical, color: SEVERITY_COLORS.critical },
        { name: labels.high,     key: 'HIGH',     value: counts.high,     color: SEVERITY_COLORS.high },
        { name: labels.medium,   key: 'MEDIUM',   value: counts.medium,   color: SEVERITY_COLORS.medium },
        { name: labels.low,      key: 'LOW',      value: counts.low,      color: SEVERITY_COLORS.low },
        { name: labels.unknown,  key: 'UNKNOWN',  value: counts.unknown,  color: SEVERITY_COLORS.unknown },
    ].filter(d => d.value > 0) // este filtro tira os valores com 0 para o grafico não desenhar fatias que não devia
}

export function SeverityDonut({ counts, total, onClickSeverity }: {
    counts: CountsBySeverity
    total: number
    onClickSeverity?: (severity: string) => void
}) {
    const { t } = useTranslation()
    const data = severityData(counts, t.stats.severities)
    const isEmpty = data.length === 0

    return (
        <div className={Style.donutWrapper}>
            <PieChart width={160} height={160}>
                {isEmpty ? (
                    <Pie data={[{ value: 1 }]} cx={75} cy={75} innerRadius={50} outerRadius={70} dataKey="value" stroke="none">
                        <Cell fill="rgba(128,128,128,0.15)" />
                    </Pie>
                ) : (
                    <Pie data={data} cx={75} cy={75} innerRadius={50} outerRadius={70} dataKey="value" stroke="none">
                        {data.map((entry, i) => (
                            <Cell
                                key={i}
                                fill={entry.color}
                                onClick={(e) => { e.stopPropagation(); onClickSeverity?.(entry.key) }}
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

export function StatCard({ title, open, fixed, dismissed, counts, onClickSeverity }: {
    title: string
    open: number
    fixed: number
    dismissed: number
    counts: CountsBySeverity
    onClickSeverity?: (severity: string) => void
}) {
    const { t } = useTranslation()
    const severityEntries = [
        { label: t.stats.severities.critical, value: counts.critical, color: SEVERITY_COLORS.critical },
        { label: t.stats.severities.high,     value: counts.high,     color: SEVERITY_COLORS.high },
        { label: t.stats.severities.medium,   value: counts.medium,   color: SEVERITY_COLORS.medium },
        { label: t.stats.severities.low,      value: counts.low,      color: SEVERITY_COLORS.low },
    ]

    return (
        <div className={Style.card}>
            <div className={Style.cardLeft}>
                <span className={Style.cardTitle}>{title}</span>
                <div className={Style.cardBadges}>
                    <span className={Style.badge} data-variant="open">{open} {t.stats.open}</span>
                    <span className={Style.badge}>{fixed} {t.stats.fixed}</span>
                    <span className={Style.badge}>{dismissed} {t.stats.dismissed}</span>
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
            <SeverityDonut counts={counts} total={open} onClickSeverity={onClickSeverity} />
        </div>
    )
}

export function HistoryLineChart({ title, data }: {
    title: string
    data: Array<{ date: string; count: number; countsBySeverity: CountsBySeverity }>
}) {
    const { t } = useTranslation()
    const severityLines = [
        { key: 'count',    label: t.stats.severities.total,    color: '#a0a0a0' },
        { key: 'critical', label: t.stats.severities.critical, color: SEVERITY_COLORS.critical },
        { key: 'high',     label: t.stats.severities.high,     color: SEVERITY_COLORS.high },
        { key: 'medium',   label: t.stats.severities.medium,   color: SEVERITY_COLORS.medium },
        { key: 'low',      label: t.stats.severities.low,      color: SEVERITY_COLORS.low },
        { key: 'unknown',  label: t.stats.severities.unknown,  color: SEVERITY_COLORS.unknown },
    ]

    const [active, setActive] = useState<Set<string>>(new Set(severityLines.map(s => s.key)))
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
                        {severityLines.map(s => (
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
                        {severityLines.map(s => (
                            <Line key={s.key} type="monotone" dataKey={s.key} stroke={s.color} strokeWidth={2} dot={{ r: 3, fill: s.color }} hide={!active.has(s.key)} />
                        ))}
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    )
}

export function StatsGrid({ children }: { children: React.ReactNode }) {
    return <div className={Style.grid}>{children}</div>
}

export function StatsSection({ children }: { children: React.ReactNode }) {
    return <div className={Style.statsSection}>{children}</div>
}
