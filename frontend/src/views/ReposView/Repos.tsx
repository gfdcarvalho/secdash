import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Repository } from '../../model/repository/repository'
import Style from './Repos.module.css'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import { AddFromGithubModal } from './AddFromGithubModal'
import { AddFromGitlabModal } from './AddFromGitlabModal'
import {Toast} from "../../components/Toast.tsx";

type OpenModal = 'github' | 'gitlab' | null

export function Repos() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const [searchParams, setSearchParams] = useSearchParams()
    const [repositories, setRepositories] = useState<Array<Repository>>()
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')
    const [toastMessage, setToastMessage] = useState<string | null>(null)
    const [openModal, setOpenModal] = useState<OpenModal>(() => {
        const provider = searchParams.get('provider')
        if (provider === 'github' || provider === 'gitlab') return provider
        return null
    })

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

    useEffect(() => {
        if (openModal === null && searchParams.has('provider')) {
            setSearchParams({}, { replace: true })
        }
    }, [openModal])

    const deleteRepo = async (repoId: number) => {
        const response = await api.delete(`/repos/${repoId}`)
        if (isSuccess(response)) {
            setRepositories(prev =>
                prev?.filter(repo => repo.rid !== repoId)
            )
        } else {
            setToastMessage(t.repoDetails.deleteError)
        }
    }

    const handleClose = () => setOpenModal(null)
    const handleRepoAdded = () => getRepositories()

    const alreadyAddedIds = new Set(repositories?.map(r => r.externalId) ?? [])

    return (
        <div className={Style.repositoriesContent}>
            {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} duration={5000} />}
            {openModal === 'github' && (
                <AddFromGithubModal onClose={handleClose} onRepoAdded={handleRepoAdded} alreadyAddedIds={alreadyAddedIds} />
            )}
            {openModal === 'gitlab' && (
                <AddFromGitlabModal onClose={handleClose} onRepoAdded={handleRepoAdded} alreadyAddedIds={alreadyAddedIds} />
            )}
            <div className={Style.topSection}>
                <h2>{t.repos.title}</h2>
                <div className={Style.topSectionButtons}>
                    <button className={Style.addRepositoriesButtons} onClick={() => setOpenModal('github')}>
                        {t.repos.addFromGithub}
                    </button>
                    <button className={Style.addRepositoriesButtons} onClick={() => setOpenModal('gitlab')}>
                        {t.repos.addFromGitlab}
                    </button>
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
                                <button
                                    className={Style.removeButton}
                                    onClick={e => {
                                        e.stopPropagation()
                                        void deleteRepo(repo.rid)
                                    }}
                                >
                                    {t.repos.deleteButton}
                                </button>
                            </div>
                        ))
                    }
                </div>
            </div>
        </div>
    )
}
