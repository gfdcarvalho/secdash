package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.repositories.ExternalGithubRepository
import com.isel.ps.secdash.model.repositories.ExternalOwner
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.users.Token
import com.isel.ps.secdash.repository.interfaces.GithubRepositoryInterface
import org.jdbi.v3.core.Handle

class GithubRepository(
    private val handle: Handle,
) : GithubRepositoryInterface {

    override fun storeRepository(
        userId: Int,
        repository: ExternalGithubRepository
    ): Repository {
        // primeiro adicionar o owner se não existir e devolver o id
        val ownerId = insertOwner(repository.externalOwner)
        // segundo adicionar o repositorio se não existir e returnar o repo
        TODO()
        // terceiro adicionar na tabela de user_repositories o userId com repoId
    }

    private fun insertOwner(owner: ExternalOwner ): Int {
        handle.createUpdate(
            """
                insert into owner (name, url, avatar_url, platform)
                values (:name, :url, :avatar_url, :platform)
                ON CONFLICT (name, platform) DO NOTHING
            """.trimIndent()
        )
            .bind("name", owner.name)
            .bind("url", owner.url)
            .bind("avatar_url", owner.avatarUrl)
            .bind("platform", owner.platform)
            .execute()

        val id = handle.createQuery(
            """
                select id from owner
                where name = :name and 
            """.trimIndent()
        )
        TODO()

    }
}