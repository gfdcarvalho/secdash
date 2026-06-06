import { PieChart, Pie, Cell, Tooltip } from 'recharts'
import { useNavigate, useParams } from 'react-router'
import type { CountsBySeverity } from '../../model/teams/teams'
import type { RepoStats } from '../../model/repository/repository'
import Style from './RepoStatsSection.module.css'

const SEVERITY_COLORS = {
    critical: '#e05555',
    high: '#e07a35',
    medium: '#e0b535',
    low: '#5ba85a',
    unknown: '#888888',
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

export function RepoStatsSection({ stats }: Props) {
    const navigate = useNavigate()
    const { repoId } = useParams<{ repoId: string }>()

    return (
        <div className={Style.statsSection}>
            <div className={Style.grid}>
                <StatCard
                    title="Vulnerabilities"
                    open={stats.vulnerabilityStats.open}
                    fixed={stats.vulnerabilityStats.fixed}
                    dismissed={stats.vulnerabilityStats.dismissed}
                    counts={stats.vulnerabilityStats.countsBySeverity}
                    onClickSeverity={(severity) =>
                        navigate(
                            `/repos/${repoId}/vulnerabilities`,
                            { state: { severity } }
                        )
                    }
                />

                <StatCard
                    title="SAST Alerts"
                    open={stats.sastStats.open}
                    fixed={stats.sastStats.fixed}
                    dismissed={stats.sastStats.dismissed}
                    counts={stats.sastStats.countsBySeverity}
                    onClickSeverity={(severity) =>
                        navigate(
                            `/repos/${repoId}/sast`,
                            { state: { severity } }
                        )
                    }
                />
            </div>
        </div>
    )
}