package com.isel.ps.secdash.repository

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
}