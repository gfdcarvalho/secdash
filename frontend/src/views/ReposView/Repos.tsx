import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { NavLink } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Repository } from '../../model/repository/repository'
import Style from './Repos.module.css'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'

export function Repos() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const [repositories, setRepositories] = useState<Array<Repository>>()
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')

    const getRepositories = async () => {
        const response = await api.get<Array<Repository>>('/repos')
        if (isSuccess(response)) {
            setRepositories(response.value.data)
        } else {
            setError(true)
        }
    }

    useEffect(() => {
        getRepositories()
    }, [])

    return (
        <div className={Style.repositoriesContent}>
            <div className={Style.topSection}>
                <h2>{t.repos.title}</h2>
                <div className={Style.topSectionButtons}>
                    <NavLink to='/repos/github' className={Style.addRepositoriesButtons}>
                        {t.repos.addFromGithub}
                    </NavLink>
                    <NavLink to='/repos/gitlab' className={Style.addRepositoriesButtons}>
                        {t.repos.addFromGitlab}
                    </NavLink>
                </div>
            </div>
            <div className={Style.bottomSection}>
                <div className={Style.searchBarDiv}>
                    <input
                        className={Style.searchBar}
                        type="text"
                        placeholder={t.repos.searchPlaceholder}
                        value={search}
                        onChange={e => setSearch(e.target.value)}
                    />
                </div>
                <div className={Style.reposListDiv}>
                    {error && <p className={Style.message}>{t.repos.error}</p>}
                    {!error && repositories === undefined && <p className={Style.message}>{t.repos.loading}</p>}
                    {repositories?.length === 0 && <p className={Style.message}>{t.repos.noRepositories}</p>}
                    {repositories
                        ?.filter(repo => repo.name.toLowerCase().includes(search.toLowerCase()))
                        .map(repo => (
                            <div key={repo.rid} className={Style.repoCard} onClick={() => navigate(`/repos/${repo.rid}`)}>
                                {repo.owner.avatarUrl
                                    ? <img className={Style.ownerAvatar} src={repo.owner.avatarUrl} alt={repo.owner.name} />
                                    : <div className={Style.ownerAvatarFallback}>{repo.owner.name.charAt(0).toUpperCase()}</div>
                                }
                                <div className={Style.repoCardInfo}>
                                    <div className={Style.repoCardHeader}>
                                        <span className={Style.repoName}>{repo.name}</span>
                                        <span className={Style.repoBadge}>{repo.visibility}</span>
                                        <span className={Style.repoBadge}>{repo.platform}</span>
                                    </div>
                                    <p className={Style.repoDescription}>{repo.description}</p>
                                    <div className={Style.repoCardMeta}>
                                        <span>{repo.owner.name}</span>
                                        <span>{t.repos.forks}: {repo.forksCount}</span>
                                        <span>{t.repos.issues}: {repo.issuesCount}</span>
                                    </div>
                                </div>
                            </div>
                        ))
                    }
                </div>
            </div>
        </div>
    )
}
