import { useEffect, useState } from 'react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Translations } from '../../i18n/I18nProvider'
import type { ExternalRepository } from '../../model/repository/externalRepository'
import Style from './Gitlab.module.css'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import { authorizeWithProvider } from '../../utils/Authenticate'


export function Gitlab() {
    const { t } = useTranslation()
    const [repositories, setRepositories] = useState<Array<ExternalRepository>>()
    const [error, setError] = useState(false)
    const [errorMessage, setErrorMessage] = useState("")
    const [search, setSearch] = useState("")

    const handleRepoClick = (_repo: ExternalRepository) => {
        // TODO: navegar para repo details — navigate(`/gitlab/repos/${_repo.externalId}`)
    }

    const getRepos = async () => {
        const response = await api.get<Array<ExternalRepository>>("gitlab/repos")
        if (isSuccess(response)) {
            setRepositories(response.value.data)
            setError(false)
        } else {
            if (response.value.status === 401) {
                authorizeWithProvider('gitlab')
            }
            if (response.value.status === 404) {
                setError(true)
                setErrorMessage(t.gitlab.error)
            }
        }
    }

    useEffect(() => {
        getRepos()
    }, [])

    return (
        <div className={Style.content}>
            <div className={Style.topSection}>
                <h2>{t.gitlab.title}</h2>
            </div>
            <div className={Style.bottomSection}>
                <div className={Style.searchBarDiv}>
                    <input className={Style.searchBar} type="text" placeholder={t.gitlab.searchPlaceholder} value={search} onChange={e => setSearch(e.target.value)}/>
                </div>
                <div className={Style.reposListDiv}>
                    {error && <RepoMessage text={errorMessage} />}
                    {!error && repositories === undefined && <RepoMessage text={t.gitlab.loading} />}
                    {repositories?.length === 0 && <RepoMessage text={t.gitlab.noRepositories} />}
                    {repositories?.filter(repo => repo.name.toLowerCase().includes(search.toLowerCase())).map(repo => repositoryCard(repo, t, handleRepoClick))}
                </div>
            </div>
        </div>
    )
}


function repositoryCard(repo: ExternalRepository, t: Translations, onRepoClick: (repo: ExternalRepository) => void) {
    const owner = repo.externalOwner
    return (
        <div key={repo.externalId} className={Style.repoCard} onClick={() => onRepoClick(repo)}>
            {owner.avatarUrl
                ? <img className={Style.ownerAvatar} src={owner.avatarUrl} alt={owner.name} />
                : <div className={Style.ownerAvatarFallback}>{owner.name.charAt(0).toUpperCase()}</div>
            }
            <div className={Style.repoCardInfo}>
                <div className={Style.repoCardHeader}>
                    <span className={Style.repoName}>{repo.name}</span>
                    <span className={Style.repoVisibility}>{repo.visibility}</span>
                </div>
                {repo.description && <p className={Style.repoDescription}>{repo.description}</p>}
                <div className={Style.repoCardMeta}>
                    <span>{owner.name}</span>
                    <span>{t.gitlab.forks}: {repo.forksCount}</span>
                    <span>{t.gitlab.issues}: {repo.issuesCount}</span>
                </div>
            </div>
            <div className={Style.repoCardActions}>
                <button className={Style.addRepoButton} onClick={e => { e.stopPropagation(); /* TODO: lógica de adicionar repositório */ }}>{t.gitlab.addRepository}</button>
                <button className={Style.addToTeamButton} onClick={e => { e.stopPropagation(); /* TODO: lógica de adicionar à equipa */ }}>{t.gitlab.addToTeam}</button>
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
