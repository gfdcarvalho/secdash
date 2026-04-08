package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.Owner
import com.isel.ps.secdash.model.repositories.ExternalGithubRepository
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositorySqlDto
import com.isel.ps.secdash.repository.interfaces.GithubRepositoryInterface
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class GithubRepository(
    private val handle: Handle,
) : GithubRepositoryInterface {

    override fun storeRepository(
        userId: Int,
        repository: ExternalGithubRepository
    ): Repository {
        val owner = insertOwner(repository.externalOwner)
        val repositoryDto = insertRepository(repository, owner.oid)
        insertIntoUserRepositories(repositoryDto.rid, userId)
        return repositoryDto.toDomainRepository(owner)
    }

    private fun insertIntoUserRepositories(
        repositoryId: Int,
        userId: Int,
    ) {
        handle.createUpdate(
            """
            insert into user_repositories(uid, rid)
            values (:uid, :rid)
        """.trimIndent()
        )
            .bind("uid", userId)
            .bind("rid", repositoryId)
            .execute()
    }

    private fun insertRepository(
        repository: ExternalGithubRepository,
        ownerId: Int
    ): RepositorySqlDto {
        handle.createUpdate(
            """
                insert into repositories ( name, external_id, platform, owner_id, html_url, description, issues_count, created_at, updated_at, forks_count, visibility)
                values ( :name, :external_id, :platform::platform, :owner_id, :html_url, :description, :issues_count, :created_at, :updated_at, :forks_count, :visibility)
                ON CONFLICT (external_id, platform) DO NOTHING
            """.trimIndent()
        )
            .bind("name", repository.name)
            .bind("external_id", repository.externalId)
            .bind("platform", repository.platform.name)
            .bind("owner_id", ownerId)
            .bind("html_url", repository.htmlUrl)
            .bind("description", repository.description)
            .bind("issues_count", repository.issuesCount)
            .bind("created_at", repository.createdAt)
            .bind("updated_at", repository.updatedAt)
            .bind("forks_count", repository.forksCount)
            .bind("visibility", repository.visibility)
            .execute()

        val repository = handle.createQuery(
            """
            select * from repositories
            where external_id = :external_id
        """.trimIndent()
        )
            .bind("external_id", repository.externalId)
            .mapTo<RepositorySqlDto>()
            .one()
        return repository
    }

    private fun insertOwner(owner: ExternalOwner): Owner {
        handle.createUpdate(
            """
                insert into owners (external_id, name, url, avatar_url, platform)
                values (:external_id, :name, :url, :avatar_url, :platform)
                ON CONFLICT (name, platform) DO NOTHING
            """.trimIndent()
        )
            .bind("external_id", owner.externalId)
            .bind("name", owner.name)
            .bind("url", owner.url)
            .bind("avatar_url", owner.avatarUrl)
            .bind("platform", owner.platform)
            .execute()

        val owner = handle.createQuery(
            """
                select * from owners
                where external_id = :external_id
            """.trimIndent()
        )
            .bind("external_id", owner.externalId)
            .mapTo<Owner>()
            .one()
        return owner
    }
}