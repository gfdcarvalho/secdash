package com.isel.ps.secdash.model.users

import com.isel.ps.secdash.model.repositories.Repository

class UserProfileInformation(
    val id: Int,
    val name: String,
    val email: String,
    val role: AppRole,
    val repositories: List<Repository>
) {
}