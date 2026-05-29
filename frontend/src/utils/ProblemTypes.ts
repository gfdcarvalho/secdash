const BASE = "https://github.com/gfdcarvalho/secdash/tree/master/docs/problems"

export const ProblemTypes = {
    // general
    internalServerError:      `${BASE}/example-problem`,
    unauthorized:             `${BASE}/example-problem`,
    notFound:                 `${BASE}/example-problem`,

    // user
    userAlreadyExists:        `${BASE}/example-problem`,
    invalidCredentials:       `${BASE}/example-problem`,
    invalidUsername:          `${BASE}/example-problem`,
    invalidEmail:             `${BASE}/example-problem`,
    invalidPassword:          `${BASE}/example-problem`,
    userNotFound:             `${BASE}/example-problem`,
    nameIsRequired:           `${BASE}/example-problem`,

    // repositories
    invalidRequest:           `${BASE}/example-problem`,
    invalidExternalId:        `${BASE}/example-problem`,
    externalIdIsRequired:     `${BASE}/example-problem`,
    repositoryNotFound:       `${BASE}/example-problem`,
    repositoryAlreadyAdded:   `${BASE}/example-problem`,
    userAuthorizationRequired:`${BASE}/example-problem`,
    ownerIsRequired:          `${BASE}/example-problem`,
    ownerNotFound:            `${BASE}/example-problem`,
    repoDoesNotHaveSastFeatureEnabled:       `${BASE}/example-problem`,
    repoDoesNotHaveDependabotFeatureEnabled: `${BASE}/example-problem`,

    // teams
    invalidTeamName:          `${BASE}/example-problem`,
    onlyTeamLeader:           `${BASE}/example-problem`,
    teamNotFound:             `${BASE}/example-problem`,
    userAlreadyOnTeam:        `${BASE}/example-problem`,
    userNotOnTeam:            `${BASE}/example-problem`,
    userAlreadyLeader:        `${BASE}/example-problem`,
} as const
