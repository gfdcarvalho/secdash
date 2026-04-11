package com.isel.ps.secdash.repository

import com.isel.ps.secdash.repository.interfaces.Transaction
import org.jdbi.v3.core.Handle

class JdbiTransaction(private val handle: Handle) : Transaction {
    override val usersRepository: UserRepository = UserRepository(handle)
    override val githubRepository: GithubRepository = GithubRepository(handle)
    override val repositoriesRepository: RepositoriesRepository = RepositoriesRepository(handle)



    override fun rollback() {
        handle.rollback()
    }
}