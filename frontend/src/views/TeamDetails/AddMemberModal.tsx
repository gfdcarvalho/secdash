import { useEffect, useState } from 'react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { PublicUser } from '../../model/user/publicUser'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import { ProblemTypes } from '../../utils/ProblemTypes'
import Style from './AddRepoModal.module.css'

type AddState = 'idle' | 'adding' | 'added' | 'error'

type Props = {
    teamId: number
    alreadyAddedIds: Set<number>
    onClose: () => void
    onMemberAdded: () => void
}

export function AddMemberModal({ teamId, alreadyAddedIds, onClose, onMemberAdded }: Props) {
    const { t } = useTranslation()
    const [users, setUsers] = useState<PublicUser[]>()
    const [search, setSearch] = useState('')
    const [addStates, setAddStates] = useState<Record<number, AddState>>({})
    const [toastMessage, setToastMessage] = useState<string | null>(null)

    useEffect(() => {
        const getUsers = async () => {
            const response = await api.get<PublicUser[]>('/users')
            if (isSuccess(response)) setUsers(response.value.data)
        }
        getUsers()
    }, [])

    const addMember = async (uid: number) => {
        setAddStates(prev => ({ ...prev, [uid]: 'adding' }))
        const res = await api.post(`/teams/${teamId}/users`, { userId: uid })
        if (isSuccess(res)) {
            setAddStates(prev => ({ ...prev, [uid]: 'added' }))
            onMemberAdded()
        } else {
            if (res.value.type === ProblemTypes.userAlreadyOnTeam) {
                setToastMessage(t.teamDetails.memberAlreadyAdded)
                setAddStates(prev => ({ ...prev, [uid]: 'idle' }))
            } else {
                setAddStates(prev => ({ ...prev, [uid]: 'error' }))
            }
        }
    }

    useEffect(() => {
        if (toastMessage === null) return
        const timer = setTimeout(() => setToastMessage(null), 3000)
        return () => clearTimeout(timer)
    }, [toastMessage])

    const available = users?.filter(
        u => !alreadyAddedIds.has(u.uid) && (
            u.name.toLowerCase().includes(search.toLowerCase()) ||
            u.email.toLowerCase().includes(search.toLowerCase())
        )
    )

    return (
        <div className={Style.overlay} onClick={e => { if (e.target === e.currentTarget) onClose() }}>
            <div className={Style.modal}>
                <div className={Style.modalHeader}>
                    <span className={Style.modalTitle}>{t.teamDetails.addMembersTitle}</span>
                    <button className={Style.closeButton} onClick={onClose}>✕</button>
                </div>

                <input
                    className={Style.search}
                    type="text"
                    placeholder={t.teamDetails.addMembersSearch}
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    autoFocus
                />

                <div className={Style.list}>
                    {users === undefined && <p className={Style.message}>{t.teamDetails.addMembersLoading}</p>}
                    {available?.length === 0 && <p className={Style.message}>{t.teamDetails.addMembersEmpty}</p>}
                    {available?.map(user => {
                        const state = addStates[user.uid] ?? 'idle'
                        return (
                            <div key={user.uid} className={Style.repoRow}>
                                <div className={Style.memberAvatarFallback}>{user.name.charAt(0).toUpperCase()}</div>
                                <div className={Style.repoInfo}>
                                    <span className={Style.repoName}>{user.name}</span>
                                    <div className={Style.repoMeta}>
                                        <span>{user.email}</span>
                                    </div>
                                </div>
                                <button
                                    className={Style.addButton}
                                    onClick={() => addMember(user.uid)}
                                    disabled={state !== 'idle'}
                                    data-state={state}
                                >
                                    {state === 'idle' && t.teamDetails.addMembersAdd}
                                    {state === 'adding' && t.teamDetails.addMembersAdding}
                                    {state === 'added' && t.teamDetails.addMembersAdded}
                                    {state === 'error' && t.teamDetails.addMembersError}
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
