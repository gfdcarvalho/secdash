import type { Repository } from "../repository/repository"

export type Team = {
    tid: number,
    name: string,
    description: string | null,
    repos: Repository[],
}

export type SimpleTeam = {
    tid: number,
    name: string,
    description: string | null,
}

export type SimpleTeamsListOutput = {
    teams: SimpleTeam[]
}
