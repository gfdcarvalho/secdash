import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import type { HistoryStats, RepoStats } from '../../model/repository/repository'
import Style from './RepoStatsSection.module.css'
import type { RepositoryVulnerabilities } from '../../model/vulnerabilities/vulnerabilities.ts'
import { api } from '../../utils/fetchApi.ts'
import { isSuccess } from '../../utils/Either.ts'
import type { RepositorySast } from '../../model/sast/sast.ts'
import { useTranslation } from '../../i18n/I18nProvider'
import { StatCard, HistoryLineChart, StatsGrid, StatsSection } from '../../components/StatsComponents'

type Props = {
    stats: RepoStats
    platform: string
    rid: number
    historyStats?: HistoryStats
}

export function RepoStatsSection({ stats, platform, rid, historyStats }: Props) {
    const { t } = useTranslation()
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
                    <StatCard title={t.stats.vulnerabilities} open={stats.vulnerabilityStats.open} fixed={stats.vulnerabilityStats.fixed} dismissed={stats.vulnerabilityStats.dismissed} counts={stats.vulnerabilityStats.countsBySeverity} onClickSeverity={handleVulnClick} />
                    <StatCard title={t.stats.sastAlerts} open={stats.sastStats.open} fixed={stats.sastStats.fixed} dismissed={stats.sastStats.dismissed} counts={stats.sastStats.countsBySeverity} onClickSeverity={handleSastClick} />
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
