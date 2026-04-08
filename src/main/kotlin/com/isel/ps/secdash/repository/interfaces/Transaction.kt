package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.repository.GithubRepository
import com.isel.ps.secdash.repository.UserRepository

interface Transaction {
    val usersRepository: UserRepository
    val githubRepository: GithubRepository


    fun rollback()
}