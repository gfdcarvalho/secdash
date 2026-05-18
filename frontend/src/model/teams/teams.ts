
export type SimpleTeam = {
    tid : number,
    name : string,
    description : string | null,
}

export type SimpleTeamsListOutput = {
    teams: SimpleTeam[]
}