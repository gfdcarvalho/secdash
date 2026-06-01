import { useEffect, useState } from 'react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Repository } from '../../model/repository/repository'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import Style from './AddRepoModal.module.css'

type AddState = 'idle' | 'adding' | 'added' | 'error'

type Props = {
    teamId: number
    alreadyAddedIds: Set<number>
    onClose: () => void
    onRepoAdded: () => void
}

export function AddRepoModal({ teamId, alreadyAddedIds, onClose, onRepoAdded }: Props) {
    const { t } = useTranslation()
    const [repos, setRepos] = useState<Repository[]>()
    const [search, setSearch] = useState('')
    const [addStates, setAddStates] = useState<Record<number, AddState>>({})

    useEffect(() => {
        const getRepos = async () => {
            const response = await api.get<Repository[]>('/repos')
            if (isSuccess(response)) setRepos(response.value.data)
        }
        getRepos()
    }, [])

    const addRepo = async (rid: number) => {
        setAddStates(prev => ({ ...prev, [rid]: 'adding' }))
        const res = await api.post(`/teams/${teamId}/repository`, { repositoryId: rid })
        if (isSuccess(res)) {
            setAddStates(prev => ({ ...prev, [rid]: 'added' }))
            onRepoAdded()
        } else {
            setAddStates(prev => ({ ...prev, [rid]: 'error' }))
        }
    }

    const available = repos?.filter(
        r => !alreadyAddedIds.has(r.rid) && r.name.toLowerCase().includes(search.toLowerCase())
    )

    return (
        <div className={Style.overlay} onClick={e => { if (e.target === e.currentTarget) onClose() }}>
            <div className={Style.modal}>
                <div className={Style.modalHeader}>
                    <span className={Style.modalTitle}>{t.teamDetails.addReposTitle}</span>
                    <button className={Style.closeButton} onClick={onClose}>✕</button>
                </div>

                <input
                    className={Style.search}
                    type="text"
                    placeholder={t.teamDetails.addReposSearch}
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    autoFocus
                />

                <div className={Style.list}>
                    {repos === undefined && <p className={Style.message}>{t.teamDetails.addReposLoading}</p>}
                    {available?.length === 0 && <p className={Style.message}>{t.teamDetails.addReposEmpty}</p>}
                    {available?.map(repo => {
                        const state = addStates[repo.rid] ?? 'idle'
                        return (
                            <div key={repo.rid} className={Style.repoRow}>
                                <div className={Style.repoInfo}>
                                    <span className={Style.repoName}>{repo.name}</span>
                                    <div className={Style.repoMeta}>
                                        <span>{repo.owner.name}</span>
                                        <span>{repo.platform}</span>
                                        <span>{repo.visibility}</span>
                                    </div>
                                </div>
                                <button
                                    className={Style.addButton}
                                    onClick={() => addRepo(repo.rid)}
                                    disabled={state !== 'idle'}
                                    data-state={state}
                                >
                                    {state === 'idle' && t.teamDetails.addReposAdd}
                                    {state === 'adding' && t.teamDetails.addReposAdding}
                                    {state === 'added' && t.teamDetails.addReposAdded}
                                    {state === 'error' && t.teamDetails.addReposError}
                                </button>
                            </div>
                        )
                    })}
                </div>
            </div>
        </div>
    )
}
