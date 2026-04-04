package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.users.PasswordValidationInfo
import com.isel.ps.secdash.model.users.User
import com.isel.ps.secdash.repository.interfaces.UserRepositoryInterface
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo


class UserRepository(
    private val handle: Handle,
) : UserRepositoryInterface {

    override fun createUser(
        username: String,
        email: String,
        password: PasswordValidationInfo
    ): Int {
        val id = handle.createUpdate("insert into users (name, password_validation, email) values (:name, :password, :email)")
            .bind("name", username)
            .bind("password", password.passwordValidation)
            .bind("email", email)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
        return id
    }

    override fun createGoogleUser(
        username: String,
        email: String,
        googleId: String
    ): User {
        val user = handle.createUpdate("insert into users(name, email, google_id) values (:name, :email, :googleId)")
            .bind("name", username)
            .bind("email", email)
            .bind("googleId", googleId)
            .executeAndReturnGeneratedKeys()
            .mapTo<User>() // this may fail needs testing !!!
            .one()
        return user
    }

    override fun getUserByUsername(
        username: String
    ): User? {
        val user = handle.createQuery("select * from users where name = :username")
            .bind("username", username)
            .mapTo<User>()
            .singleOrNull()
        return user
    }

    override fun getUserByEmail(email: String): User? {
        val user = handle.createQuery("select * from users where email = :email")
            .bind("email", email)
            .mapTo<User>()
            .singleOrNull()
        return user
    }

    override fun storeToken(
        token: String,
        maxTokens: Int
    ) {

    }
}