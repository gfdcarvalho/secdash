import { useEffect, useState } from 'react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { ExternalRepository } from '../../model/repository/externalRepository'
import Style from './Github.module.css'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import { authorizeWithProvider } from '../../utils/Authenticate'


export function Github() {
    const { t } = useTranslation()
    const [repositories, setRepositories] = useState<Array<ExternalRepository>>()
    const [error, setError] = useState(false)
    const [errorMessage, setErrorMessage] = useState("")
    const [search, setSearch] = useState("")

    const getRepos = async () => {
        const response = await api.get<Array<ExternalRepository>>("github/repos")
        if (isSuccess(response)) {
            setRepositories(response.value.data)
            setError(false)
        } else {
            if(response.value.status === 401) {
                authorizeWithProvider('github')
            }
            if (response.value.status === 404) {
                setError(true)
                setErrorMessage("teste!!")
            }
        }
    }

    useEffect(() => {
        getRepos()
    }, [])

    return (
        <div className={Style.content}>
            <div className={Style.topSection}>
                <h2>Github</h2>
            </div>
            <div className={Style.bottomSection}>
                <div className={Style.searchBarDiv}>
                    <input className={Style.searchBar} type="text" placeholder="search in your github repositories" value={search} onChange={e => setSearch(e.target.value)}/>
                </div>
                <div className={Style.reposListDiv}>
                    {error && <RepoMessage text={errorMessage} />}
                    {!error && repositories === undefined && <RepoMessage text="Loading..." />}
                    {repositories?.length === 0 && <RepoMessage text="No repositories found." />}
                    {repositories?.filter(repo => repo.name.toLowerCase().includes(search.toLowerCase())).map(repo => repositoryCard(repo))}
                </div>
            </div>
        </div>
    )
}


function repositoryCard(repo: ExternalRepository) {
    return (
        <div key={repo.externalId} className={Style.repoCard}>
            <div className={Style.repoCardInfo}>
                <div className={Style.repoCardHeader}>
                    <span className={Style.repoName}>{repo.name}</span>
                    <span className={Style.repoVisibility}>{repo.visibility}</span>
                </div>
                {repo.description && <p className={Style.repoDescription}>{repo.description}</p>}
                <div className={Style.repoCardMeta}>
                    <span>{repo.externalOwner.name}</span>
                    <span>Forks: {repo.forksCount}</span>
                    <span>Issues: {repo.issuesCount}</span>
                </div>
            </div>
            <div className={Style.repoCardActions}>
                <button className={Style.addRepoButton}>Add Repository</button>
                <button className={Style.addToTeamButton}>Add to Team</button>
            </div>
        </div>
    )
}


function RepoMessage({ text }: { text: string }) {
    return (
        <div className={Style.repoMessage}>
            <p>{text}</p>
        </div>
    )
}
