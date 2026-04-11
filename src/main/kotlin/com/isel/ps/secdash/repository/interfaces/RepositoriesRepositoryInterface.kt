package com.isel.ps.secdash.repository.interfaces

interface RepositoriesRepositoryInterface {

    fun userHasAccessToRepository(
        userId: Int,
        rid: Int
    ): Boolean

    fun getRepositoryFullName(rid: Int): String?
}