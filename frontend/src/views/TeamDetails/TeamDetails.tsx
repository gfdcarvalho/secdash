import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Team } from '../../model/teams/teams'
import type { Repository } from '../../model/repository/repository'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import Style from './TeamDetails.module.css'
import { AddRepoModal } from './AddRepoModal'


export function TeamDetails() {
    const { t } = useTranslation()
    const { teamId } = useParams()
    const navigate = useNavigate()
    const [team, setTeam] = useState<Team>()
    const [notFound, setNotFound] = useState(false)
    const [showAddModal, setShowAddModal] = useState(false)

    const getTeam = async () => {
        const response = await api.get<Team>(`/teams/${teamId}`)
        if (isSuccess(response)) {
            setTeam(response.value.data)
        } else {
            setNotFound(true)
        }
    }

    useEffect(() => {
        getTeam()
    }, [teamId])

    if (notFound) {
        return (
            <div className={Style.content}>
                <p className={Style.message}>{t.teamDetails.notFound}</p>
            </div>
        )
    }

    if (!team) {
        return (
            <div className={Style.content}>
                <p className={Style.message}>{t.teamDetails.loading}</p>
            </div>
        )
    }

    return (
        <div className={Style.content}>
            <div className={Style.topSection}>
                <span className={Style.teamName}>{team.name}</span>
                {team.description && <p className={Style.description}>{team.description}</p>}
            </div>
            <div className={Style.bottomSection}>
                <div className={Style.section}>
                    <h3 className={Style.sectionTitle}>{t.teamDetails.members} ({team.members.length})</h3>
                    <div className={Style.membersList}>
                        {team.members.map(member => (
                            <div key={member.uid} className={Style.memberCard}>
                                <div className={Style.memberAvatarFallback}>{member.name.charAt(0).toUpperCase()}</div>
                                <div className={Style.memberInfo}>
                                    <span className={Style.memberName}>{member.name}</span>
                                    <span className={Style.memberEmail}>{member.email}</span>
                                </div>
                                <span className={Style.roleBadge}>{member.teamRole.toLowerCase()}</span>
                            </div>
                        ))}
                    </div>
                </div>

                <div className={Style.section}>
                    <div className={Style.sectionHeader}>
                        <h3 className={Style.sectionTitle}>{t.teamDetails.repositories} ({team.repos.length})</h3>
                        <button className={Style.addReposButton} onClick={() => setShowAddModal(true)}>
                            {t.teamDetails.addRepos}
                        </button>
                    </div>
                    <div className={Style.reposList}>
                        {team.repos.length === 0 && <p className={Style.emptyMessage}>{t.teamDetails.noRepositories}</p>}
                        {team.repos.map(repo => repoCard(
                            repo,
                            t.teamDetails.removeFromTeam,
                            () => navigate(`/repos/${repo.rid}`),
                            async () => {
                                await api.delete(`/teams/${team.tid}/repository/${repo.rid}`)
                                getTeam()
                            }
                        ))}
                    </div>
                </div>
            </div>

            {showAddModal && (
                <AddRepoModal
                    teamId={team.tid}
                    alreadyAddedIds={new Set(team.repos.map(r => r.rid))}
                    onClose={() => setShowAddModal(false)}
                    onRepoAdded={getTeam}
                />
            )}
        </div>
    )
}


function repoCard(repo: Repository, removeLabel: string, onClick: () => void, onRemove: () => void) {
    const owner = repo.owner
    return (
        <div key={repo.rid} className={Style.repoCard} onClick={onClick}>
            {owner.avatarUrl
                ? <img className={Style.ownerAvatar} src={owner.avatarUrl} alt={owner.name} />
                : <div className={Style.ownerAvatarFallback}>{owner.name.charAt(0).toUpperCase()}</div>
            }
            <div className={Style.repoCardInfo}>
                <div className={Style.repoCardHeader}>
                    <span className={Style.repoName}>{repo.name}</span>
                    <span className={Style.repoVisibility}>{repo.visibility}</span>
                </div>
                <p className={Style.repoDescription}>{repo.description}</p>
                <div className={Style.repoCardMeta}>
                    <span>{owner.name}</span>
                    <span>{repo.platform}</span>
                </div>
            </div>
            <button
                className={Style.removeButton}
                onClick={e => { e.stopPropagation(); onRemove() }}
            >
                {removeLabel}
            </button>
        </div>
    )
}
