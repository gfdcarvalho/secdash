const BASE = "https://github.com/gfdcarvalho/secdash/tree/master/docs/problems"

export const ProblemTypes = {
    // general
    internalServerError:      `${BASE}/internal-server-error`,
    unauthorized:             `${BASE}/unauthorized`,
    notFound:                 `${BASE}/not-found`,

    // user
    userAlreadyExists:        `${BASE}/user-already-exists`,
    invalidCredentials:       `${BASE}/invalid-credentials`,
    invalidUsername:          `${BASE}/invalid-username`,
    invalidEmail:             `${BASE}/invalid-email`,
    invalidPassword:          `${BASE}/invalid-password`,
    userNotFound:             `${BASE}/user-not-found`,
    nameIsRequired:           `${BASE}/name-is-required`,

    // repositories
    invalidRequest:           `${BASE}/invalid-request`,
    invalidExternalId:        `${BASE}/invalid-external-id`,
    externalIdIsRequired:     `${BASE}/external-id-is-required`,
    repositoryNotFound:       `${BASE}/repository-not-found`,
    repositoryAlreadyAdded:   `${BASE}/repository-already-added`,
    userAuthorizationRequired:`${BASE}/user-authorization-required`,
    ownerIsRequired:          `${BASE}/owner-is-required`,
    ownerNotFound:            `${BASE}/owner-not-found`,
    repoDoesNotHaveSastFeatureEnabled:       `${BASE}/repo-does-not-have-sast-feature-enabled`,
    repoDoesNotHaveDependabotFeatureEnabled: `${BASE}/repo-does-not-have-dependabot-feature-enabled`,

    // teams
    invalidTeamName:          `${BASE}/invalid-team-name`,
    onlyTeamLeader:           `${BASE}/only-team-leader`,
    teamNotFound:             `${BASE}/team-not-found`,
    userAlreadyOnTeam:        `${BASE}/user-already-on-team`,
    userNotOnTeam:            `${BASE}/user-not-on-team`,
    userAlreadyLeader:        `${BASE}/user-already-leader`,
    forbidden:                `${BASE}/forbidden`,
} as const