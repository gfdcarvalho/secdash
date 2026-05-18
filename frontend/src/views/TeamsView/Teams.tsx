import { useEffect, useState } from 'react'
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
                <h2> Teams</h2>
                <button> Create New Team</button>
            </div>
            <div className={Style.buttomSection}>
                <div className={Style.searchBarDiv}>
                    <input className={Style.searchBar} type="text" placeholder="search in your teams"/>
                </div>
                <div className={Style.teamsListDiv}>
                    {error && <p>Failed to load teams</p>}
                    {!error && teams === undefined && <p>Loading...</p>}
                    {teams?.length === 0 && <p>No teams yet</p>}
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