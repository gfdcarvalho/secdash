import { useState } from 'react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Repository } from '../../model/repository/repository'
import Style from './Repos.module.css'
import { api } from '../../utils/fetchApi'
import { NavLink } from 'react-router'


export function Repos() {
    const { t } = useTranslation()
    const [repositories, setRepositories] = useState<Array<Repository>>()
    const [error, setError] = useState(false)

    // const getRepositories = async () => {
    //     const response = await api.get<>
    // }
    // este endpoint ainda não existe mas temos que criar para ir buscar todos os repos que um useer tem no nosso dominio


    return (
        <div className={Style.repositoriesContent}>
            <div className={Style.topSection}>
                <h2> Repos </h2>
                <div className={Style.topSectionButtons}>
                    <NavLink to='/repos/github' className={Style.addRepositoriesButtons}>  
                        Add repositories from GitHub 
                    </NavLink>
                    <NavLink to='/repos/gitlab' className={Style.addRepositoriesButtons}>  
                        Add repositories from GitLab 
                    </NavLink>
                </div>
            </div>
            <div className={Style.bottomSection}>
                <div className={Style.searchBarDiv}>
                    <input className={Style.searchBar} type="text" placeholder="Search your repositories"/>
                </div>
                <div className={Style.reposListDiv}>

                </div>
            </div>

        </div>
    )
}