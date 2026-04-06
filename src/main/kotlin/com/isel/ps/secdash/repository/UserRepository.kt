package com.isel.ps.secdash.repository

import com.isel.ps.secdash.model.users.PasswordValidationInfo
import com.isel.ps.secdash.model.users.Token
import com.isel.ps.secdash.model.users.TokenInfo
import com.isel.ps.secdash.model.users.User
import com.isel.ps.secdash.repository.interfaces.UserRepositoryInterface
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory


class UserRepository(
    private val handle: Handle,
) : UserRepositoryInterface {

    override fun createUser(
        username: String,
        email: String,
        password: PasswordValidationInfo
    ): Int {
        val id =
            handle.createUpdate("insert into users (name, password_validation, email) values (:name, :password, :email)")
                .bind("name", username)
                .bind("password", password.passwordValidation)
                .bind("email", email)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()
        return id
    }

    override fun storeToken(
        token: Token,
        maxTokens: Int
    ) {
        val deletions = handle.createUpdate(
            """
                    delete from tokens 
                    where user_id = :user_id 
                        and token_validation in (
                            select token_validation from tokens where user_id = :user_id 
                                order by last_used_at desc offset :offset
                        )
                    """.trimIndent(),
        )
            .bind("user_id", token.userId)
            .bind("offset", maxTokens - 1)
            .execute()

        //logger.info("{} tokens deleted when creating new token", deletions)

        handle.createUpdate(
            """
            insert into tokens(user_id, token_validation, created_at, last_used_at) 
            values (:user_id, :token_validation, :created_at, :last_used_at)
            """.trimIndent(),
        )
            .bind("user_id", token.userId)
            .bind("token_validation", token.tokenInfo.token)
            .bind("created_at", token.createdAt.epochSeconds)
            .bind("last_used_at", token.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun getTokenByTokenValidationInfo(tokenInfo: TokenInfo): Pair<User, Token>? =
        handle.createQuery(
            """
                select uid, name, email, password_validation, token_validation, created_at, last_used_at
                from users
                inner join tokens
                on users.uid = tokens.user_id
                where token_validation = :validation_information
            """,
            )
            .bind("validation_information", tokenInfo.token)
            .mapTo<UserAndTokenModel>()
            .singleOrNull()
            ?.userAndToken

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        handle.createUpdate(
            """
            update tokens
            set last_used_at = :last_used_at
            where token_validation = :validation_information
            """.trimIndent(),
        )
            .bind("last_used_at", now.epochSeconds)
            .bind("validation_information", token.tokenInfo.token)
            .execute()
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


    private data class UserAndTokenModel(
        val uid: Int,
        val name: String,
        val email: String,
        val passwordValidation: String?,
        val tokenValidation: TokenInfo,
        val createdAt: Long,
        val lastUsedAt: Long,
    ) {
        val userAndToken: Pair<User, Token>
            get() =
                Pair(
                    User(uid, name, email, passwordValidation),
                    Token(
                        tokenValidation,
                        uid,
                        Instant.fromEpochSeconds(createdAt),
                        Instant.fromEpochSeconds(lastUsedAt),
                    ),
                )
    }

}