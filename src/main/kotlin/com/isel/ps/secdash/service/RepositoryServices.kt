package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import org.springframework.stereotype.Service

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
}