package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.repository.GithubRepository
import com.isel.ps.secdash.repository.GitlabRepository
import com.isel.ps.secdash.repository.RepositoriesRepository
import com.isel.ps.secdash.repository.TeamRepository
import com.isel.ps.secdash.repository.UserRepository

interface Transaction {
    val usersRepository: UserRepository
    val githubRepository: GithubRepository
    val gitlabRepository: GitlabRepository
    val repositoriesRepository: RepositoriesRepository
    val teamRepository: TeamRepository

    fun rollback()
}