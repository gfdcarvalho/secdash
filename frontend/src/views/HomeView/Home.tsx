import { useEffect, useState } from "react"
import { useNavigate } from "react-router"
import { useTranslation } from "../../i18n/I18nProvider"
import Style from "./Home.module.css"
import type { SimpleTeamWithCount, SimpleTeamWithCountListOutput } from "../../model/teams/teams"
import { isSuccess } from "../../utils/Either"
import { api } from "../../utils/fetchApi"


export function Home() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const [teams, setTeams] = useState<Array<SimpleTeamWithCount>>()
    const [error, setError] = useState(false)

    const getTeams = async () => {
        const response = await api.get<SimpleTeamWithCountListOutput>("/teams")
        if (isSuccess(response)) {
            setTeams(response.value.data.teams)
            setError(false)
        } else {
            setError(true)
        }
    }

    useEffect(() => {
        getTeams()
    }, [])

    return (
        <div className={Style.homeContent}>
            <div className={Style.topSection}>
                <h2>{t.home.title}</h2>
            </div>
            <div className={Style.bottomSection}>
                <div className={Style.searchBarDiv}>
                    <input className={Style.searchBar} type="text" placeholder={t.teams.searchPlaceholder} />
                </div>
                <div className={Style.teamsListDiv}>
                    {error && <HomeMessage text={t.home.error} />}
                    {!error && teams === undefined && <HomeMessage text={t.home.loading} />}
                    {teams?.length === 0 && <HomeMessage text={t.home.noTeams} />}
                    {teams?.map(team => (
                        <div key={team.tid} className={Style.teamCard} onClick={() => navigate(`/teams/${team.tid}`)}>
                            <div className={Style.teamInfo}>
                                <h3>{team.name}</h3>
                                {team.description && <p>{team.description}</p>}
                            </div>
                            <div className={Style.teamCounts}>
                                <div className={Style.countItem}>
                                    <span className={Style.countNumber}>{team.memberCount}</span>
                                    <span className={Style.countLabel}>{t.home.members}</span>
                                </div>
                                <div className={Style.countItem}>
                                    <span className={Style.countNumber}>{team.repoCount}</span>
                                    <span className={Style.countLabel}>{t.home.repositories}</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}


function HomeMessage({ text }: { text: string }) {
    return (
        <div className={Style.messageCard}>
            <p>{text}</p>
        </div>
    )
}
