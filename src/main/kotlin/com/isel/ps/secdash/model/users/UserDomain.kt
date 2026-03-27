package com.isel.ps.secdash.model.users

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserDomain(
    private val passwordEncoder: PasswordEncoder,
){

    fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(passwordValidation = passwordEncoder.encode(password)!!) // !! because password cant be null maybe when we add password validation we can revisit this

}