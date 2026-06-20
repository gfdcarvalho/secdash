import { useNavigate } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import type { CountsBySeverity, RepoProblemCount } from '../../model/teams/teams'
import type { Repository } from '../../model/repository/repository'
import { SEVERITY_COLORS } from '../../utils/statsConstants'
import { StatsSection, StatsGrid } from '../../components/StatsComponents'
import Style from './TopProblemRepos.module.css'

const WEIGHTS = { critical: 10, high: 5, medium: 2, low: 1, unknown: 0 } // depois podemos mudar isto se percebermos que não esta bem ...

function score(c: CountsBySeverity) {
    return c.critical * WEIGHTS.critical
        + c.high * WEIGHTS.high
        + c.medium * WEIGHTS.medium
        + c.low * WEIGHTS.low
        + c.unknown * WEIGHTS.unknown
}

function total(c: CountsBySeverity) {
    return c.critical + c.high + c.medium + c.low + c.unknown
}

type Props = {
    repoCounts: RepoProblemCount[]
    repos: Repository[]
}

export function TopProblemRepos({ repoCounts, repos }: Props) {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const repoById = new Map(repos.map(r => [r.rid, r]))

    // ordenamos pelo score por severidade (não pela contagem bruta) e tiramos os que ficam a 0 nesse tipo
    const rank = (pick: (r: RepoProblemCount) => CountsBySeverity) =>
        repoCounts
            .filter(r => score(pick(r)) > 0)
            .sort((a, b) => score(pick(b)) - score(pick(a)))
            .slice(0, 5)

    const renderList = (pick: (r: RepoProblemCount) => CountsBySeverity, title: string) => {
        const entries = rank(pick)
        return (
            <div className={Style.card}>
                <span className={Style.cardTitle}>{title}</span>
                {entries.length === 0
                    ? <p className={Style.empty}>{t.topRepos.empty}</p>
                    : (
                        <div className={Style.list}>
                            {entries.map((entry, i) => {
                                const repo = repoById.get(entry.rid)
                                const counts = pick(entry)
                                const dots = ([
                                    ['critical', counts.critical],
                                    ['high', counts.high],
                                    ['medium', counts.medium],
                                    ['low', counts.low],
                                ] as const).filter(([, v]) => v > 0)
                                return (
                                    <div key={entry.rid} className={Style.row} onClick={() => navigate(`/repos/${entry.rid}`)}>
                                        <span className={Style.rank}>{i + 1}</span>
                                        <span className={Style.name}>{repo?.name ?? `#${entry.rid}`}</span>
                                        <div className={Style.dots}>
                                            {dots.map(([sev, v]) => (
                                                <span key={sev} className={Style.dotGroup}>
                                                    <span className={Style.dot} style={{ background: SEVERITY_COLORS[sev] }} />
                                                    {v}
                                                </span>
                                            ))}
                                        </div>
                                        <span className={Style.total}>{total(counts)} {t.topRepos.total}</span>
                                    </div>
                                )
                            })}
                        </div>
                    )
                }
            </div>
        )
    }

    return (
        <StatsSection>
            <StatsGrid>
                {renderList(r => r.vulnerabilities, t.topRepos.vulnTitle)}
                {renderList(r => r.sastAlerts, t.topRepos.sastTitle)}
            </StatsGrid>
        </StatsSection>
    )
}