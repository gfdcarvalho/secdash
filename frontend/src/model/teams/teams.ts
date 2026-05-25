import type { Repository } from "../repository/repository"

export type Team = {
    tid: number,
    name: string,
    description: string | null,
    repos: Repository[],
    members: TeamMember[],
}

export type TeamMember = {
    uid: number,
    name: string,
    email: string,
    teamRole: TeamRole,
}

export type TeamRole = "LEADER" | "COLLABORATOR"

export type SimpleTeam = {
    tid: number,
    name: string,
    description: string | null,
}

export type SimpleTeamsListOutput = {
    teams: SimpleTeam[]
}
