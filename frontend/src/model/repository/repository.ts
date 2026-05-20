export type Platform = "GITHUB" | "GITLAB"

export type Visibility = "PUBLIC" | "PRIVATE" | "INTERNAL"

export type Owner = {
    oid: number,
    externalId: string,
    name: string,
    url: string,
    avatarUrl: string | null,
    platform: Platform,
}

export type Repository = {
    rid: number,
    name: string,
    externalId: string,
    platform: Platform,
    owner: Owner,
    htmlUrl: string,
    description: string,
    issuesCount: number,
    createdAt: string,
    updatedAt: string,
    forksCount: number,
    visibility: Visibility,
}
