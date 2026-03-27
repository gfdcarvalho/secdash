package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.users.PasswordValidationInfo

interface UserRepositoryInterface {

    fun createUser(
        username: String,
        email: String,
        password: PasswordValidationInfo,
    ): Int // maybe we should create a dto for this return
}