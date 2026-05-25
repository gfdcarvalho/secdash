package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure
import org.springframework.stereotype.Service

sealed class GetRepositoryError{
    data object NotFound : GetRepositoryError()
    data object Unauthorized : GetRepositoryError()
}

typealias GetRepositoryResult = Either<GetRepositoryError, Repository>

@Service
class RepositoryServices(
    private val transactionManager: TransactionManager,
) {
    fun findAllByUser(uid: Int): List<Repository> {
        return transactionManager.run {
            val reposRepo = it.repositoriesRepository
            reposRepo.findAllByUser(uid)
        }
    }

    fun getRepositoryById(
        rid: Int,
        uid: Int
    ): GetRepositoryResult {
        return transactionManager.run {
            val reposirotyRepo = it.repositoriesRepository


            // verificar se o repo existe
            if (!reposirotyRepo.checkRepositoryExistence(rid)) return@run failure(GetRepositoryError.NotFound)
            // verificar se ele tem autorização para o repo
            if (!reposirotyRepo.userHasAccessToRepository(uid, rid)) return@run failure(GetRepositoryError.Unauthorized)


            // get repo
            val repo = reposirotyRepo.getRepositoryById(rid)
            success(repo)
        }
    }
}