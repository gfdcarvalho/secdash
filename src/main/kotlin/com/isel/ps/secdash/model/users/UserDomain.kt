package com.isel.ps.secdash.model.users

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class UserDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: Sha256TokenEncoder,
    private val config: UsersDomainConfig,
){
    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
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

    fun createTokenValidationInformation(token: String): TokenInfo = tokenEncoder.createValidationInformation(token)


}