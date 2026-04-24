package com.isel.ps.secdash

import com.isel.ps.secdash.model.Owner
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.vulnerability.ExternalVulnerability
import com.isel.ps.secdash.model.vulnerability.RepositoryVulnerabilities
import java.time.Instant

fun testExternalRepository(
    name: String = "testRepository0",
    externalId: String = "12",
    platform: Platform,
    owner: ExternalOwner = testExternalOwner(platform = platform),
    htmlUrl: String = "https://www.example.com",
    description: String = "test",
    issuesCount: Int = 0,
    createdAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    updatedAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    forksCount: Int = 0,
    visibility: Repository.Visibility = Repository.Visibility.PUBLIC,
) = ExternalRepository(
    name,
    externalId,
    platform,
    owner,
    htmlUrl,
    description,
    issuesCount,
    createdAt,
    updatedAt,
    forksCount,
    visibility
)


fun testExternalOwner(
    externalId: String = "123",
    name: String = "owner",
    url: String = "https://example.com/owner/repo",
    avatarUrl: String = "https://example.com/owner/avatar",
    platform: Platform,
) = ExternalOwner(externalId, name, url, avatarUrl, platform)


fun testRepository(
    rid: Int = 1,
    name: String = "test-repo",
    externalId: String = "123",
    platform: Platform,
    owner: Owner = testOwner(platform = platform),
    htmlUrl: String = "https://example.com/owner/repo",
    description: String = "test description",
    issuesCount: Int = 0,
    createdAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    updatedAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    forksCount: Int = 0,
    visibility: Repository.Visibility = Repository.Visibility.PUBLIC,
) = Repository(
    rid,
    name,
    externalId,
    platform,
    owner,
    htmlUrl,
    description,
    issuesCount,
    createdAt,
    updatedAt,
    forksCount,
    visibility
)

fun testOwner(
    oid: Int = 1,
    externalId: String = "123",
    name: String = "owner",
    url: String = "https://example.com/owner/repo",
    avatarUrl: String = "https://example.com/owner/avatar",
    platform: Platform,
) = Owner(oid, externalId, name, url, avatarUrl, platform)

fun testRepositoryCreationDto(
    name: String = "testRepository0",
    externalId: String = "12",
    platform: Platform,
    owner: ExternalOwner = testExternalOwner(platform = platform),
    htmlUrl: String = "https://www.example.com",
    description: String = "test",
    issuesCount: Int = 0,
    createdAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    updatedAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    forksCount: Int = 0,
    visibility: Repository.Visibility = Repository.Visibility.PUBLIC,
) = RepositoryCreationDto(
    name,
    externalId,
    owner,
    htmlUrl,
    description,
    issuesCount,
    createdAt.toString(),
    updatedAt.toString(),
    forksCount,
    visibility
)

fun testRepositoryVulnerabilities(
    rid: Int = 1,
    vulnerabilities: List<ExternalVulnerability> = emptyList<ExternalVulnerability>(),
) = RepositoryVulnerabilities(rid, vulnerabilities)

fun testExternalVulnerabilities() = emptyList<ExternalVulnerability>()