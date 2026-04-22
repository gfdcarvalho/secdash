package com.isel.ps.secdash.model.users

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class UserDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: Sha256TokenEncoder,
    private val config: UsersDomainConfig,
) {
    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun canBeToken(token: String): Boolean =
        try {
            Base64.getUrlDecoder()
                .decode(token).size == config.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(passwordValidation = passwordEncoder.encode(password)!!) // !! because password cant be null maybe when we add password validation we can revisit this

    fun validatePassword(
        password: String,
        validationInfo: String,
    ) = passwordEncoder.matches(
        password,
        validationInfo,
    )

    fun createTokenValidationInformation(token: String): TokenValidationInfo = tokenEncoder.createValidationInformation(token)

    fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.now()
        return token.createdAt <= now &&
                (now - token.createdAt) <= config.tokenTtl &&
                (now - token.lastUsedAt) <= config.tokenRollingTtl
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun validUsername(username: String): Boolean = !username.isBlank()

    fun validEmail(email: String): Boolean = !email.isBlank()

    fun validPassword(password: String): Boolean = !password.isBlank()


    val maxNumberOfTokensPerUser = config.maxTokensPerUser
}