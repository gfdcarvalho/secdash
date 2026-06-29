import { useEffect, useState } from 'react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { ExternalRepository } from '../../model/repository/externalRepository'
import type { Repository } from '../../model/repository/repository'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import { authorizeWithProvider } from '../../utils/Authenticate'
import { ProblemTypes } from '../../utils/ProblemTypes'
import Style from './AddFromProviderModal.module.css'

type AddState = 'idle' | 'adding' | 'added' | 'error'

type Props = {
    onClose: () => void
    onRepoAdded: () => void
    alreadyAddedIds: Set<string>
}

export function AddFromGitlabModal({ onClose, onRepoAdded, alreadyAddedIds }: Props) {
    const { t } = useTranslation()
    const [repositories, setRepositories] = useState<ExternalRepository[]>()
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')
    const [addStates, setAddStates] = useState<Record<string, AddState>>({})
    const [toastMessage, setToastMessage] = useState<string | null>(null)
    const [linkRepo, setLinkRepo] = useState<ExternalRepository | null>(null)
    const [linkSearching, setLinkSearching] = useState(false)

    useEffect(() => {
        const getRepos = async () => {
            const response = await api.get<ExternalRepository[]>('/gitlab/repos')
            if (isSuccess(response)) {
                setRepositories(response.value.data)
                setError(false)
            } else {
                if (response.value.status === 401) {
                    authorizeWithProvider('gitlab', `${window.location.origin}/repos?provider=gitlab`)
                }
                if (response.value.status === 404) {
                    setError(true)
                }
            }
        }
        getRepos()
    }, [])

    const handleSearchByLink = async () => {
        if (!search.trim()) return
        setLinkSearching(true)
        setLinkRepo(null)
        const response = await api.get<ExternalRepository>(`/gitlab/repository?link=${encodeURIComponent(search.trim())}`)
        if (isSuccess(response)) {
            setLinkRepo(response.value.data)
            setError(false)
        } else {
            setError(true)
        }
        setLinkSearching(false)
    }

    const handleSearchChange = (value: string) => {
        setSearch(value)
        if (!value.trim()) {
            setLinkRepo(null)
            setError(false)
        }
    }

    const handleAddRepo = async (repo: ExternalRepository) => {
        setAddStates(prev => ({ ...prev, [repo.externalId]: 'adding' }))
        const response = await api.post<Repository>('/gitlab/repositories', repo)
        if (isSuccess(response)) {
            setAddStates(prev => ({ ...prev, [repo.externalId]: 'added' }))
            onRepoAdded()
        } else {
            if (response.value.type === ProblemTypes.repositoryAlreadyAdded) {
                setToastMessage(t.gitlab.repositoryAlreadyAdded)
                setAddStates(prev => ({ ...prev, [repo.externalId]: 'idle' }))
            } else {
                setAddStates(prev => ({ ...prev, [repo.externalId]: 'error' }))
            }
        }
    }

    useEffect(() => {
        if (toastMessage === null) return
        const timer = setTimeout(() => setToastMessage(null), 3000)
        return () => clearTimeout(timer)
    }, [toastMessage])

    const filtered = linkRepo
        ? [linkRepo]
        : repositories
            ?.filter(r => !alreadyAddedIds.has(r.externalId))
            .filter(r => r.name.toLowerCase().includes(search.toLowerCase()))

    return (
        <div className={Style.overlay} onClick={e => { if (e.target === e.currentTarget) onClose() }}>
            <div className={Style.modal}>
                <div className={Style.modalHeader}>
                    <span className={Style.modalTitle}>{t.gitlab.title}</span>
                    <button className={Style.closeButton} onClick={onClose}>✕</button>
                </div>

                <div className={Style.searchRow}>
                    <input
                        className={Style.search}
                        type="text"
                        placeholder={t.gitlab.searchPlaceholder}
                        value={search}
                        onChange={e => handleSearchChange(e.target.value)}
                        onKeyDown={e => { if (e.key === 'Enter') handleSearchByLink() }}
                        autoFocus
                    />
                    <button
                        className={Style.linkSearchButton}
                        onClick={handleSearchByLink}
                        disabled={linkSearching || !search.trim()}
                        title={t.gitlab.searchByLink}
                    >
                        {linkSearching ? t.gitlab.loading : t.gitlab.searchByLink}
                    </button>
                </div>

                <div className={Style.list}>
                    {error && <p className={Style.message}>{t.gitlab.error}</p>}
                    {!error && repositories === undefined && <p className={Style.message}>{t.gitlab.loading}</p>}
                    {!error && filtered?.length === 0 && <p className={Style.message}>{t.gitlab.noRepositories}</p>}
                    {filtered?.map(repo => {
                        const owner = repo.externalOwner
                        const state = addStates[repo.externalId] ?? 'idle'
                        return (
                            <div key={repo.externalId} className={Style.repoRow}>
                                {owner.avatarUrl
                                    ? <img className={Style.ownerAvatar} src={owner.avatarUrl} alt={owner.name} />
                                    : <div className={Style.ownerAvatarFallback}>{owner.name.charAt(0).toUpperCase()}</div>
                                }
                                <div className={Style.repoInfo}>
                                    <span className={Style.repoName}>{repo.name}</span>
                                    <div className={Style.repoMeta}>
                                        <span>{owner.name}</span>
                                        <span>{repo.visibility}</span>
                                        <span>{t.gitlab.forks}: {repo.forksCount}</span>
                                        <span>{t.gitlab.issues}: {repo.issuesCount}</span>
                                    </div>
                                </div>
                                <button
                                    className={Style.addButton}
                                    onClick={() => handleAddRepo(repo)}
                                    disabled={state !== 'idle'}
                                    data-state={state}
                                >
                                    {state === 'idle' && t.gitlab.addRepository}
                                    {state === 'adding' && t.gitlab.adding}
                                    {state === 'added' && t.gitlab.added}
                                    {state === 'error' && t.gitlab.addError}
                                </button>
                            </div>
                        )
                    })}
                </div>

                {toastMessage && <div className={Style.toast}>{toastMessage}</div>}
            </div>
        </div>
    )
}
