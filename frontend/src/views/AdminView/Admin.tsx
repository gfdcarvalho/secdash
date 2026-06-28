import { useEffect, useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import { useAuthentication } from '../../utils/Authentication'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import type { Repository } from '../../model/repository/repository'
import type { SimpleTeamWithCount, SimpleTeamWithCountListOutput } from '../../model/teams/teams'
import type { UserOutputDto } from '../../model/user/user'
import { Toast } from '../../components/Toast'
import Style from './Admin.module.css'

type Tab = 'users' | 'repositories' | 'teams'

export function Admin() {
    const { t } = useTranslation()
    const [user] = useAuthentication()
    const navigate = useNavigate()
    const [tab, setTab] = useState<Tab>('users')
    const [toastMessage, setToastMessage] = useState<string | null>(null)

    if (user && user.role !== 'ADMIN') {
        return <Navigate to="/" replace />
    }

    return (
        <div className={Style.adminContent}>
            {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} duration={5000} />}
            <div className={Style.topSection}>
                <h2>{t.admin.title}</h2>
            </div>
            <div className={Style.tabBar}>
                <button
                    className={Style.tabButton}
                    data-active={tab === 'users'}
                    onClick={() => setTab('users')}
                >
                    {t.admin.tabs.users}
                </button>
                <button
                    className={Style.tabButton}
                    data-active={tab === 'repositories'}
                    onClick={() => setTab('repositories')}
                >
                    {t.admin.tabs.repositories}
                </button>
                <button
                    className={Style.tabButton}
                    data-active={tab === 'teams'}
                    onClick={() => setTab('teams')}
                >
                    {t.admin.tabs.teams}
                </button>
            </div>
            {tab === 'users' && <UsersTab onError={setToastMessage} />}
            {tab === 'repositories' && <RepositoriesTab navigate={navigate} onError={setToastMessage} />}
            {tab === 'teams' && <TeamsTab navigate={navigate} onError={setToastMessage} />}
        </div>
    )
}

type ErrorHandler = (message: string) => void

function UsersTab({ onError }: { onError: ErrorHandler }) {
    const { t } = useTranslation()
    const [users, setUsers] = useState<Array<UserOutputDto>>()
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')

    const getUsers = async () => {
        const response = await api.get<Array<UserOutputDto>>('/users')
        if (isSuccess(response)) {
            setUsers(response.value.data)
            setError(false)
        } else {
            setError(true)
        }
    }

    useEffect(() => {
        getUsers()
    }, [])

    const promoteUser = async (uid: number) => {
        const response = await api.post(`/admin/promote-user/${uid}`)
        if (isSuccess(response)) {
            setUsers(prev => prev?.map(u => (u.uid === uid ? { ...u, role: 'ADMIN' } : u)))
        } else {
            onError(t.admin.users.promoteError)
        }
    }

    const deleteUser = async (uid: number) => {
        const response = await api.delete(`/admin/delete-user/${uid}`)
        if (isSuccess(response)) {
            setUsers(prev => prev?.filter(u => u.uid !== uid))
        } else {
            onError(t.admin.users.deleteError)
        }
    }

    return (
        <div className={Style.bottomSection}>
            <div className={Style.searchBarDiv}>
                <input
                    className={Style.searchBar}
                    type="text"
                    placeholder={t.admin.users.searchPlaceholder}
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
            </div>
            <div className={Style.listDiv}>
                {error && <p className={Style.message}>{t.admin.users.error}</p>}
                {!error && users === undefined && <p className={Style.message}>{t.admin.users.loading}</p>}
                {users?.length === 0 && <p className={Style.message}>{t.admin.users.empty}</p>}
                {users
                    ?.filter(u => u.name.toLowerCase().includes(search.toLowerCase()))
                    .map(u => (
                        <div key={u.uid} className={Style.staticCard}>
                            <div className={Style.ownerAvatarFallback}>{u.name.charAt(0).toUpperCase()}</div>
                            <div className={Style.cardInfo}>
                                <div className={Style.cardHeader}>
                                    <span className={Style.cardName}>{u.name}</span>
                                    <span className={Style.badge}>{u.role}</span>
                                </div>
                                <div className={Style.cardMeta}>
                                    <span>{u.email}</span>
                                </div>
                            </div>
                            <div className={Style.cardActions}>
                                {u.role === 'USER' && (
                                    <button
                                        className={Style.promoteButton}
                                        onClick={() => void promoteUser(u.uid)}
                                    >
                                        {t.admin.users.promote}
                                    </button>
                                )}
                                <button
                                    className={Style.removeButton}
                                    onClick={() => void deleteUser(u.uid)}
                                >
                                    {t.admin.users.delete}
                                </button>
                            </div>
                        </div>
                    ))}
            </div>
        </div>
    )
}

function RepositoriesTab({ navigate, onError }: { navigate: ReturnType<typeof useNavigate>; onError: ErrorHandler }) {
    const { t } = useTranslation()
    const [repositories, setRepositories] = useState<Array<Repository>>()
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')

    const getRepositories = async () => {
        const response = await api.get<Array<Repository>>('/admin/repositories')
        if (isSuccess(response)) {
            setRepositories(response.value.data)
            setError(false)
        } else {
            setError(true)
        }
    }

    useEffect(() => {
        getRepositories()
    }, [])

    const deleteRepo = async (rid: number) => {
        const response = await api.delete(`/repos/${rid}`)
        if (isSuccess(response)) {
            setRepositories(prev => prev?.filter(r => r.rid !== rid))
        } else {
            onError(t.admin.repositories.deleteError)
        }
    }

    return (
        <div className={Style.bottomSection}>
            <div className={Style.searchBarDiv}>
                <input
                    className={Style.searchBar}
                    type="text"
                    placeholder={t.admin.repositories.searchPlaceholder}
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
            </div>
            <div className={Style.listDiv}>
                {error && <p className={Style.message}>{t.admin.repositories.error}</p>}
                {!error && repositories === undefined && <p className={Style.message}>{t.admin.repositories.loading}</p>}
                {repositories?.length === 0 && <p className={Style.message}>{t.admin.repositories.empty}</p>}
                {repositories
                    ?.filter(r => r.name.toLowerCase().includes(search.toLowerCase()))
                    .map(repo => (
                        <div key={repo.rid} className={Style.card} onClick={() => navigate(`/repos/${repo.rid}`)}>
                            {repo.owner.avatarUrl
                                ? <img className={Style.ownerAvatar} src={repo.owner.avatarUrl} alt={repo.owner.name} />
                                : <div className={Style.ownerAvatarFallback}>{repo.owner.name.charAt(0).toUpperCase()}</div>
                            }
                            <div className={Style.cardInfo}>
                                <div className={Style.cardHeader}>
                                    <span className={Style.cardName}>{repo.name}</span>
                                    <span className={Style.badge}>{repo.visibility}</span>
                                    <span className={Style.badge}>{repo.platform}</span>
                                </div>
                                <p className={Style.cardDescription}>{repo.description}</p>
                                <div className={Style.cardMeta}>
                                    <span>{repo.owner.name}</span>
                                    <span>{t.admin.repositories.forks}: {repo.forksCount}</span>
                                    <span>{t.admin.repositories.issues}: {repo.issuesCount}</span>
                                </div>
                            </div>
                            <div className={Style.cardActions}>
                                <button
                                    className={Style.removeButton}
                                    onClick={e => {
                                        e.stopPropagation()
                                        void deleteRepo(repo.rid)
                                    }}
                                >
                                    {t.admin.repositories.delete}
                                </button>
                            </div>
                        </div>
                    ))}
            </div>
        </div>
    )
}

function TeamsTab({ navigate, onError }: { navigate: ReturnType<typeof useNavigate>; onError: ErrorHandler }) {
    const { t } = useTranslation()
    const [teams, setTeams] = useState<Array<SimpleTeamWithCount>>()
    const [error, setError] = useState(false)
    const [search, setSearch] = useState('')

    const getTeams = async () => {
        const response = await api.get<SimpleTeamWithCountListOutput>('/admin/teams')
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

    const deleteTeam = async (tid: number) => {
        const response = await api.delete(`/admin/delete-team/${tid}`)
        if (isSuccess(response)) {
            setTeams(prev => prev?.filter(team => team.tid !== tid))
        } else {
            onError(t.admin.teams.deleteError)
        }
    }

    return (
        <div className={Style.bottomSection}>
            <div className={Style.searchBarDiv}>
                <input
                    className={Style.searchBar}
                    type="text"
                    placeholder={t.admin.teams.searchPlaceholder}
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
            </div>
            <div className={Style.listDiv}>
                {error && <p className={Style.message}>{t.admin.teams.error}</p>}
                {!error && teams === undefined && <p className={Style.message}>{t.admin.teams.loading}</p>}
                {teams?.length === 0 && <p className={Style.message}>{t.admin.teams.empty}</p>}
                {teams
                    ?.filter(team => team.name.toLowerCase().includes(search.toLowerCase()))
                    .map(team => (
                        <div key={team.tid} className={Style.card} onClick={() => navigate(`/teams/${team.tid}`)}>
                            <div className={Style.ownerAvatarFallback}>{team.name.charAt(0).toUpperCase()}</div>
                            <div className={Style.cardInfo}>
                                <div className={Style.cardHeader}>
                                    <span className={Style.cardName}>{team.name}</span>
                                </div>
                                {team.description && <p className={Style.cardDescription}>{team.description}</p>}
                                <div className={Style.cardMeta}>
                                    <span>{t.admin.teams.members}: {team.memberCount}</span>
                                    <span>{t.admin.teams.repositories}: {team.repoCount}</span>
                                </div>
                            </div>
                            <div className={Style.cardActions}>
                                <button
                                    className={Style.removeButton}
                                    onClick={e => {
                                        e.stopPropagation()
                                        void deleteTeam(team.tid)
                                    }}
                                >
                                    {t.admin.teams.delete}
                                </button>
                            </div>
                        </div>
                    ))}
            </div>
        </div>
    )
}
