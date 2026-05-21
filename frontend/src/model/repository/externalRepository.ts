import type { Platform, Visibility } from "./repository"

export type ExternalOwner = {
    externalId: string,
    name: string,
    url: string,
    avatarUrl: string | null,
    platform: Platform,
}

export type ExternalRepository = {
    name: string,
    externalId: string,
    platform: Platform,
    externalOwner: ExternalOwner,
    htmlUrl: string,
    description: string | null,
    issuesCount: number,
    createdAt: string,
    updatedAt: string,
    forksCount: number,
    visibility: Visibility,
}
