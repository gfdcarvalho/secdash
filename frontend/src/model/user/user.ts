import type { Repository } from '../repository/repository'
import type { SimpleTeam } from '../teams/teams'

export type AppRole = "ADMIN" | "USER"

export type User = {
    id: number
    name: string
    email: string
    role: AppRole
    repositories: Array<Repository>
    teams: Array<SimpleTeam>
}
