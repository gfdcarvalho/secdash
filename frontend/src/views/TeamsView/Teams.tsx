import { useEffect, useState } from 'react'
import { NavLink } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import Style from './Teams.module.css'
import type { SimpleTeam, SimpleTeamsListOutput } from '../../model/teams/teams'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'


export function Teams() {
    const { t } = useTranslation()
    const [teams, setTeams] = useState<Array<SimpleTeam>>()
    const [error, setError] = useState(false)

    const getTeams = async () => {
        const response = await api.get<SimpleTeamsListOutput>("/teams")
        if(isSuccess(response)) {
            setTeams(response.value.data.teams)
            setError(false)
        }else {
            setError(true)
        }
    }

    useEffect(() =>{
        getTeams() 
    }, [])


    return (
        <div className={Style.teamsContent}>
            <div className={Style.topSection}>
                <h2> {t.teams.title}</h2>
                <NavLink to='/teams/create' className={Style.createTeamButton}>
                    {t.teams.createTeam}
                </NavLink>
            </div>
            <div className={Style.bottomSection}>
                <div className={Style.searchBarDiv}>
                    <input className={Style.searchBar} type="text" placeholder={t.teams.searchPlaceholder}/>
                </div>
                <div className={Style.teamsListDiv}>
                    {error && <TeamsMessage text={t.teams.error} />}
                    {!error && teams === undefined && <TeamsMessage text={t.teams.loading} />}
                    {teams?.length === 0 && <TeamsMessage text={t.teams.noTeams} />}
                    {teams?.map(team => (
                        <div key={team.tid} className={Style.teamCard}>
                            <h3>{team.name}</h3>
                            {team.description && <p>{team.description}</p>}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}


function TeamsMessage({ text }: { text: string }) {
    return (
        <div className={Style.errorMessageCard}>
            <p>{text}</p>
        </div>
    )
}