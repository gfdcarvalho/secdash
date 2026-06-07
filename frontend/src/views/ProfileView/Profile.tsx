import { useEffect } from 'react'
import { useNavigate } from 'react-router'
import { useTranslation } from '../../i18n/I18nProvider'
import { useAuthentication } from '../../utils/Authentication'
import type { Repository } from '../../model/repository/repository'
import type { SimpleTeamWithCount } from '../../model/teams/teams'
import type { User } from '../../model/user/user'
import { api } from '../../utils/fetchApi'
import { isSuccess } from '../../utils/Either'
import Style from './Profile.module.css'


export function Profile() {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const [user, setUser] = useAuthentication()

    const refreshUser = async () => {
        const response = await api.get<User>("/users/me")
        if (isSuccess(response)) {
            setUser(response.value.data)
        }
    }

    useEffect(() => {
        refreshUser()
    }, [])

    return (
        <div className={Style.content}>
            <div className={Style.bottomSection}>
                <div className={Style.userCard}>
                    <div className={Style.userHeader}>
                        <span className={Style.userName}>{user?.name}</span>
                        <span className={Style.roleBadge}>{user?.role}</span>
                    </div>
                    <span className={Style.userEmail}>{user?.email}</span>
                </div>

                <div className={Style.section}>
                    <h3 className={Style.sectionTitle}>{t.profile.repositories} ({user?.repositories.length ?? 0})</h3>
                    <div className={Style.reposListDiv}>
                        {user?.repositories.length === 0 && <ListMessage text={t.profile.noRepositories} />}
                        {user?.repositories.map(repo => repositoryCard(repo, () => navigate(`/repos/${repo.rid}`)))}
                    </div>
                </div>

                <div className={Style.section}>
                    <h3 className={Style.sectionTitle}>{t.profile.teams} ({user?.teams.length ?? 0})</h3>
                    <div className={Style.reposListDiv}>
                        {user?.teams.length === 0 && <ListMessage text={t.profile.noTeams} />}
                        {user?.teams.map(team => teamCard(team, () => navigate(`/teams/${team.tid}`), t.home.members, t.home.repositories))}
                    </div>
                </div>
            </div>
        </div>
    )
}

function repositoryCard(repo: Repository, onClick: () => void) {
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
                {repo.description && <p className={Style.repoDescription}>{repo.description}</p>}
                <div className={Style.repoCardMeta}>
                    <span>{owner.name}</span>
                    <span>{repo.platform}</span>
                </div>
            </div>
        </div>
    )
}


function teamCard(team: SimpleTeamWithCount, onClick: () => void, membersLabel: string, repositoriesLabel: string) {
    return (
        <div key={team.tid} className={Style.teamCard} onClick={onClick}>
            <div className={Style.teamInfo}>
                <span className={Style.teamName}>{team.name}</span>
                {team.description && <p className={Style.teamDescription}>{team.description}</p>}
            </div>
            <div className={Style.teamCounts}>
                <div className={Style.countItem}>
                    <span className={Style.countNumber}>{team.memberCount}</span>
                    <span className={Style.countLabel}>{membersLabel}</span>
                </div>
                <div className={Style.countItem}>
                    <span className={Style.countNumber}>{team.repoCount}</span>
                    <span className={Style.countLabel}>{repositoriesLabel}</span>
                </div>
            </div>
        </div>
    )
}


function ListMessage({ text }: { text: string }) {
    return (
        <div className={Style.repoMessage}>
            <p>{text}</p>
        </div>
    )
}
