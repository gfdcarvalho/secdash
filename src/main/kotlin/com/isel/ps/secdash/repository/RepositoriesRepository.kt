package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.Owner
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositorySqlDto
import com.isel.ps.secdash.model.repositories.RepositoryWithOwnerSqlDto
import com.isel.ps.secdash.repository.interfaces.RepositoriesRepositoryInterface
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class RepositoriesRepository(
    private val handle: Handle,
): RepositoriesRepositoryInterface {

    override fun userHasAccessToRepository(userId: Int, rid: Int): Boolean {
        val resultRid = handle.createQuery(
            "SELECT rid FROM user_repositories WHERE uid = :userId AND rid = :rid"
        )
            .bind("userId", userId)
            .bind("rid", rid)
            .mapTo<Int>()
            .singleOrNull()
        return resultRid != null
    }

    override fun getRepositoryFullName(rid: Int): String? {
        val fullName = handle.createQuery(
            "SELECT name FROM repositories WHERE rid = :rid"
        )
            .bind("rid", rid)
            .mapTo<String>()
            .singleOrNull()
        return fullName
    }

    override fun userAlreadyHasRepo(userId: Int, repoName: String): Boolean {
        val resultRid = handle.createQuery(
            """
            SELECT r.rid FROM repositories r
            JOIN user_repositories ur ON r.rid = ur.rid
            WHERE ur.uid = :userId AND r.name = :repoName
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("repoName", repoName)
            .mapTo<Int>()
            .singleOrNull()
        return resultRid != null
    }

    override fun userAlreadyHasRepoByExternalId(userId: Int, externalId: String, platform: Platform): Boolean {
        val resultRid = handle.createQuery(
            """
            SELECT r.rid FROM repositories r
            JOIN user_repositories ur ON r.rid = ur.rid
            WHERE ur.uid = :userId AND r.external_id = :externalId AND r.platform = :platform::platform
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("externalId", externalId)
            .bind("platform", platform.name)
            .mapTo<Int>()
            .singleOrNull()
        return resultRid != null
    }

    override fun storeRepository(
        userId: Int,
        repository: ExternalRepository
    ): Repository {
        val owner = insertOwner(repository.externalOwner)
        val repositorySqlDto = insertRepository(repository, owner.oid)
        insertIntoUserRepositories(repositorySqlDto.rid, userId)
        return repositorySqlDto.toDomainRepository(owner)
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
        repository: ExternalRepository,
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
                ON CONFLICT (external_id, platform) DO NOTHING
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

    override fun getExternalId(rid: Int): String? {
        val externalId = handle.createQuery(
        """
            select external_id from repositories
            where rid = :rid
        """.trimIndent()
        )
            .bind("rid", rid)
            .mapTo<String>()
            .singleOrNull()

        return externalId
    }

    override fun findAllByUser(uid: Int): List<Repository> {
        return handle.createQuery(
            """
        SELECT
            r.rid, r.name, r.external_id, r.platform, r.owner_id,
            r.html_url, r.description, r.issues_count,
            r.created_at, r.updated_at, r.forks_count, r.visibility,
            o.oid, o.external_id AS o_external_id, o.name AS o_name,
            o.url, o.avatar_url, o.platform AS o_platform
        FROM repositories r
        JOIN user_repositories ur ON r.rid = ur.rid
        JOIN owners o ON r.owner_id = o.oid
        WHERE ur.uid = :uid
        """.trimIndent()
        )
            .bind("uid", uid)
            .mapTo<RepositoryWithOwnerSqlDto>()
            .list()
            .map { it.toDomain() }
    }
}