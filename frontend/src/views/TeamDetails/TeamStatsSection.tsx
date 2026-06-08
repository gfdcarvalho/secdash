import { useState } from 'react'
import type { HistoryStats, TeamStats } from '../../model/teams/teams'
import { useNavigate, useParams } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import { StatCard, HistoryLineChart, StatsGrid, StatsSection } from '../../components/StatsComponents'
import Style from './TeamStatsSection.module.css'

type Props = {
    stats: TeamStats
    historyStats?: HistoryStats
}

export function TeamStatsSection({ stats, historyStats }: Props) {
    const { t } = useTranslation()
    const [view, setView] = useState<'pie' | 'line'>('pie')
    const { teamId } = useParams<{ teamId: string }>()
    const navigate = useNavigate()

    return (
        <StatsSection>
            <div className={Style.toggleBar}>
                <button className={Style.toggleBtn} data-active={view === 'pie'} onClick={() => setView('pie')}>
                    {t.stats.overview}
                </button>
                <button className={Style.toggleBtn} data-active={view === 'line'} onClick={() => setView('line')}>
                    {t.stats.history}
                </button>
            </div>

            {view === 'pie' && (
                <StatsGrid>
                    <StatCard
                        title={t.stats.vulnerabilities}
                        open={stats.vulnerabilityStats.open}
                        fixed={stats.vulnerabilityStats.fixed}
                        dismissed={stats.vulnerabilityStats.dismissed}
                        counts={stats.vulnerabilityStats.countsBySeverity}
                        onClickSeverity={(severity) => navigate(`/teams/${teamId}/vulnerabilities`, { state: { severity } })}
                    />
                    <StatCard
                        title={t.stats.sastAlerts}
                        open={stats.sastStats.open}
                        fixed={stats.sastStats.fixed}
                        dismissed={stats.sastStats.dismissed}
                        counts={stats.sastStats.countsBySeverity}
                        onClickSeverity={(severity) => navigate(`/teams/${teamId}/sast`, { state: { severity } })}
                    />
                </StatsGrid>
            )}

            {view === 'line' && (
                <StatsGrid>
                    {historyStats ? (
                        <>
                            <HistoryLineChart title={t.stats.vulnerabilities} data={historyStats.vulnList} />
                            <HistoryLineChart title={t.stats.sastAlerts} data={historyStats.sastList} />
                        </>
                    ) : (
                        <p className={Style.loadingMessage}>{t.stats.loadingHistory}</p>
                    )}
                </StatsGrid>
            )}
        </StatsSection>
    )
}
