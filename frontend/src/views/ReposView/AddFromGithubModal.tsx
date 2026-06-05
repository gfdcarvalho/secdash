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

export function AddFromGithubModal({ onClose, onRepoAdded, alreadyAddedIds }: Props) {
    const { t } = useTranslation()
    const [repositories, setRepositories] = useState<ExternalRepository[]>()
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')
    const [addStates, setAddStates] = useState<Record<string, AddState>>({})
    const [toastMessage, setToastMessage] = useState<string | null>(null)

    useEffect(() => {
        const getRepos = async () => {
            const response = await api.get<ExternalRepository[]>('/github/repos')
            if (isSuccess(response)) {
                setRepositories(response.value.data)
                setError(false)
            } else {
                if (response.value.status === 401) {
                    authorizeWithProvider('github', `${window.location.origin}/repos?provider=github`)
                }
                if (response.value.status === 404) {
                    setError(true)
                }
            }
        }
        getRepos()
    }, [])

    const handleAddRepo = async (repo: ExternalRepository) => {
        setAddStates(prev => ({ ...prev, [repo.externalId]: 'adding' }))
        const response = await api.post<Repository>('/github/repositories', repo)
        if (isSuccess(response)) {
            setAddStates(prev => ({ ...prev, [repo.externalId]: 'added' }))
            onRepoAdded()
        } else {
            if (response.value.type === ProblemTypes.repositoryAlreadyAdded) {
                setToastMessage(t.github.repositoryAlreadyAdded)
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

    const filtered = repositories
        ?.filter(r => !alreadyAddedIds.has(r.externalId))
        .filter(r => r.name.toLowerCase().includes(search.toLowerCase()))

    return (
        <div className={Style.overlay} onClick={e => { if (e.target === e.currentTarget) onClose() }}>
            <div className={Style.modal}>
                <div className={Style.modalHeader}>
                    <span className={Style.modalTitle}>{t.github.title}</span>
                    <button className={Style.closeButton} onClick={onClose}>✕</button>
                </div>

                <input
                    className={Style.search}
                    type="text"
                    placeholder={t.github.searchPlaceholder}
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    autoFocus
                />

                <div className={Style.list}>
                    {error && <p className={Style.message}>{t.github.error}</p>}
                    {!error && repositories === undefined && <p className={Style.message}>{t.github.loading}</p>}
                    {!error && filtered?.length === 0 && <p className={Style.message}>{t.github.noRepositories}</p>}
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
                                        <span>{t.github.forks}: {repo.forksCount}</span>
                                        <span>{t.github.issues}: {repo.issuesCount}</span>
                                    </div>
                                </div>
                                <button
                                    className={Style.addButton}
                                    onClick={() => handleAddRepo(repo)}
                                    disabled={state !== 'idle'}
                                    data-state={state}
                                >
                                    {state === 'idle' && t.github.addRepository}
                                    {state === 'adding' && t.github.adding}
                                    {state === 'added' && t.github.added}
                                    {state === 'error' && t.github.addError}
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
