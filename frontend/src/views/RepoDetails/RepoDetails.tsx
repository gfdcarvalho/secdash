import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import type {
    DailySastCountList,
    DailyVulnerabilityCountList,
    HistoryStats,
    Repository,
    RepoStats
} from '../../model/repository/repository'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import Style from './RepoDetails.module.css'
import type { RepositoryVulnerabilities } from '../../model/vulnerabilities/vulnerabilities'
import { ProblemTypes } from '../../utils/ProblemTypes'
import type { RepositorySast } from '../../model/sast/sast'
import { Toast } from '../../components/Toast'
import { authorizeWithProvider } from '../../utils/Authenticate'
import { RepoStatsSection } from './RepoStatsSection'

export function RepoDetails() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const { repoId } = useParams()
    const [repo, setRepo] = useState<Repository>()
    const [notFound, setNotFound] = useState(false)

    const [stats, setStats] = useState<RepoStats>()
    const [historyStats, setHistoryStats] = useState<HistoryStats>()

    const [toastMessage, setToastMessage] = useState<string | null>(null)

    const getRepo = async () => {
        const response = await api.get<Repository>(`/repos/${repoId}`)
        if (isSuccess(response)) {
            setRepo(response.value.data)
        } else {
            setNotFound(true)
        }
    }

    const getStats = async () => {
        const response = await api.get<RepoStats>(`/repos/${repoId}/stats`)
        if (isSuccess(response)) setStats(response.value.data)
    }

    const getHistory = async () => {
        const sastResponse = await api.get<DailySastCountList>(`/repos/${repoId}/sast/history`)
        if (isSuccess(sastResponse)) {
            const vulnResponse = await api.get<DailyVulnerabilityCountList>(`/repos/${repoId}/vulnerability/history`)
            if (isSuccess(vulnResponse)) {
                setHistoryStats({ sastList: vulnResponse.value.data, vulnList: vulnResponse.value.data })
            }
        }
    }

    useEffect(() => {
        getRepo()
        getStats()
        getHistory()
    }, [repoId])

    if (notFound) {
        return (
            <div className={Style.content}>
                <p className={Style.message}>{t.repoDetails.notFound}</p>
            </div>
        )
    }

    if (!repo) {
        return (
            <div className={Style.content}>
                <p className={Style.message}>{t.repoDetails.loading}</p>
            </div>
        )
    }

    const deleteRepo = async () => {
        const response = await api.delete(`/repos/${repoId}`)
        if (isSuccess(response)) {
            navigate(`/repos`)
        } else {
            setToastMessage(t.repoDetails.deleteError)
        }
    }

    const owner = repo.owner
    const platform = repo.platform
    const createdAt = new Date(repo.createdAt).toLocaleDateString()
    const updatedAt = new Date(repo.updatedAt).toLocaleDateString()

    const getDependabot = async () => {
        let uri 
        if(platform === 'GITHUB'){
            uri = `/github/repositories/${repo.rid}/dependabot`
        }else {
            uri = `/gitlab/repositories/${repo.rid}/dependency-scanning`
        }
        const response = await api.get<RepositoryVulnerabilities>(uri)
        if (isSuccess(response)) {
            navigate(`/repos/${repo.rid}/vulnerabilities/${platform.toLowerCase()}`, { state: {vulnerabilities: response.value}})
        } else {
            switch(response.value.type) {
                case ProblemTypes.unauthorized:
                    authorizeWithProvider(platform.toLowerCase() as 'github' | 'gitlab', window.location.href)
                    break
                case ProblemTypes.repoDoesNotHaveDependabotFeatureEnabled:
                    setToastMessage(t.repoDetails.dependabotNotEnabled)
            }
        }

    }
    
    const getSast = async () => {
        let uri 
        if (platform ==='GITHUB') {
            uri = `/github/repositories/${repoId}/sast`
        } else {
            uri = `/gitlab/repositories/${repoId}/sast`
        }
        const response = await api.get<RepositorySast>(uri)
        if (isSuccess(response)) {
            navigate(`/repos/${repo.rid}/sast/${platform.toLowerCase()}`, { state: {sastAlerts: response.value}})
        } else {
            switch(response.value.type) {
                case ProblemTypes.unauthorized:
                    authorizeWithProvider(platform.toLowerCase() as 'github' | 'gitlab', window.location.href)
                    break
                case ProblemTypes.repoDoesNotHaveSastFeatureEnabled:
                    setToastMessage(t.repoDetails.sastNotEnabled)
            }
        }
    }

    return (
        <div className={Style.content}>
            {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} duration={5000} />}
            <div className={Style.topSection}>
                <div className={Style.topSectionLeft}>
                    <span className={Style.repoName}>{repo.name}</span>
                    <span className={Style.visibilityBadge}>{repo.visibility}</span>
                    <span className={Style.platformBadge}>{repo.platform}</span>
                </div>
                <div className={Style.buttons}>
                    <a className={Style.externalLink} href={repo.htmlUrl} target="_blank" rel="noreferrer">
                        {t.repoDetails.externalLink} {repo.platform}
                    </a>
                    <button onClick={deleteRepo} className={Style.deleteButton}>
                        {t.repoDetails.deleteButton}
                    </button>
                </div>
            </div>
            <div className={Style.bottomSection}>
                <div className={Style.ownerRow}>
                    {owner.avatarUrl
                        ? <img className={Style.ownerAvatar} src={owner.avatarUrl} alt={owner.name} />
                        : <div className={Style.ownerAvatarFallback}>{owner.name.charAt(0).toUpperCase()}</div>
                    }
                    <span className={Style.ownerName}>{owner.name}</span>
                </div>

                {repo.description && <p className={Style.description}>{repo.description}</p>}

                <div className={Style.statsGrid}>
                    <div className={Style.statItem}>
                        <span className={Style.statLabel}>{t.repoDetails.forks}</span>
                        <span className={Style.statValue}>{repo.forksCount}</span>
                    </div>
                    <div className={Style.statItem}>
                        <span className={Style.statLabel}>{t.repoDetails.issues}</span>
                        <span className={Style.statValue}>{repo.issuesCount}</span>
                    </div>
                    <div className={Style.statItem}>
                        <span className={Style.statLabel}>{t.repoDetails.createdAt}</span>
                        <span className={Style.statValue}>{createdAt}</span>
                    </div>
                    <div className={Style.statItem}>
                        <span className={Style.statLabel}>{t.repoDetails.updatedAt}</span>
                        <span className={Style.statValue}>{updatedAt}</span>
                    </div>
                </div>

                <div className={Style.actions}>
                    <button className={Style.reportsButtons} onClick={() => {getDependabot()}}>
                        {platform === 'GITHUB' ? t.repoDetails.dependabotReport : t.repoDetails.dependencyScanningReport}
                    </button>
                    <button className={Style.reportsButtons} onClick={() => {getSast()}}>
                        {platform === 'GITHUB' ? t.repoDetails.codeScanningReport : t.repoDetails.sastReport}
                    </button>
                </div>

                {stats && (
                    <RepoStatsSection
                        stats={stats}
                        platform={platform}
                        rid={repo.rid}
                        historyStats={historyStats} />
                )}
            </div>
        </div>
    )
}
