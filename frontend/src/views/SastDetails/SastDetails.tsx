import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import { type SastAlertDetail } from '../../model/sast/sast'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import Style from './SastDetails.module.css'

const SEVERITY_CLASS: Record<string, string> = {
    CRITICAL: Style.severity_critical,
    HIGH:     Style.severity_high,
    MEDIUM:   Style.severity_medium,
    LOW:      Style.severity_low,
    UNKNOWN:  Style.severity_unknown,
}

const STATE_CLASS: Record<string, string> = {
    OPEN:      Style.state_open,
    FIXED:     Style.state_fixed,
    DISMISSED: Style.state_dismissed,
}

export function SastDetails() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const { sastId } = useParams<{ sastId: string }>()

    const [alert, setAlert] = useState<SastAlertDetail | null>(null)
    const [notFound, setNotFound] = useState(false)

    useEffect(() => {
        const fetch = async () => {
            const response = await api.get<SastAlertDetail>(`/sast/${sastId}`)
            if (isSuccess(response)) {
                setAlert(response.value.data)
            } else {
                setNotFound(true)
            }
        }
        fetch()
    }, [sastId])

    if (notFound) {
        return (
            <div className={Style.content}>
                <div className={Style.topSection}>
                    <button className={Style.backButton} onClick={() => navigate(-1)}>
                        {t.sastDetails.backButton}
                    </button>
                </div>
                <p className={Style.message}>{t.sastDetails.notFound}</p>
            </div>
        )
    }

    if (!alert) {
        return (
            <div className={Style.content}>
                <p className={Style.message}>{t.sastDetails.loading}</p>
            </div>
        )
    }

    const fileLocation = () => {
        if (!alert.filePath) return null
        if (alert.startLine) {
            return alert.endLine && alert.endLine !== alert.startLine
                ? `${alert.filePath}:${alert.startLine}–${alert.endLine}`
                : `${alert.filePath}:${alert.startLine}`
        }
        return alert.filePath
    }

    return (
        <div className={Style.content}>
            <div className={Style.topSection}>
                <button className={Style.backButton} onClick={() => navigate(-1)}>
                    {t.sastDetails.backButton}
                </button>
                <div className={Style.badges}>
                    <span className={`${Style.severityBadge} ${SEVERITY_CLASS[alert.severity] ?? ''}`}>
                        {alert.severity}
                    </span>
                    <span className={`${Style.stateBadge} ${STATE_CLASS[alert.state] ?? ''}`}>
                        {alert.state}
                    </span>
                </div>
            </div>

            <div className={Style.bottomSection}>
                <h2 className={Style.title}>{alert.ruleDescription}</h2>

                {/* Repository */}
                <div className={Style.section}>
                    <h3 className={Style.sectionTitle}>{t.sastDetails.repository}</h3>
                    <div className={Style.repoRow}>
                        {alert.ownerAvatarUrl
                            ? <img className={Style.ownerAvatar} src={alert.ownerAvatarUrl} alt={alert.ownerName} />
                            : <div className={Style.ownerAvatarFallback}>{alert.ownerName.charAt(0).toUpperCase()}</div>
                        }
                        <div className={Style.repoInfo}>
                            <a className={Style.repoLink} href={alert.repoHtmlUrl} target="_blank" rel="noreferrer">
                                {alert.repoName}
                            </a>
                            <span className={Style.ownerName}>{alert.ownerName}</span>
                        </div>
                        <span className={Style.platformBadge}>{alert.platform}</span>
                    </div>
                </div>

                {/* Message */}
                {alert.message && (
                    <div className={Style.section}>
                        <h3 className={Style.sectionTitle}>{t.sastDetails.message}</h3>
                        <p className={Style.description}>{alert.message}</p>
                    </div>
                )}

                {/* Rule & Tool */}
                <div className={Style.section}>
                    <h3 className={Style.sectionTitle}>{t.sastDetails.rule}</h3>
                    <div className={Style.detailsGrid}>
                        <MetaItem label={t.sastDetails.ruleId} value={alert.ruleId} />
                        <MetaItem label={t.sastDetails.tool} value={alert.toolName} />
                        {fileLocation() && (
                            <MetaItem label={t.sastDetails.file} value={fileLocation()!} />
                        )}
                    </div>
                </div>

                {/* Dates & Link */}
                <div className={Style.section}>
                    <h3 className={Style.sectionTitle}>{t.sastDetails.details}</h3>
                    <div className={Style.detailsGrid}>
                        {alert.detectedAt && (
                            <MetaItem label={t.sastDetails.detectedAt} value={new Date(alert.detectedAt).toLocaleDateString()} />
                        )}
                        {alert.updatedAt && (
                            <MetaItem label={t.sastDetails.updatedAt} value={new Date(alert.updatedAt).toLocaleDateString()} />
                        )}
                    </div>
                    <a className={Style.externalLink} href={alert.htmlUrl} target="_blank" rel="noreferrer">
                        {t.sastDetails.openAlert}
                    </a>
                </div>
            </div>
        </div>
    )
}

function MetaItem({ label, value }: { label: string; value: string }) {
    return (
        <div className={Style.metaItem}>
            <span className={Style.metaLabel}>{label}</span>
            <span className={Style.metaValue}>{value}</span>
        </div>
    )
}
