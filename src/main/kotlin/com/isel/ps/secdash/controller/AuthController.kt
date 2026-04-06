package com.isel.ps.secdash.controller

import com.isel.ps.secdash.model.users.UserLoginDto
import com.isel.ps.secdash.service.AuthServices
import com.isel.ps.secdash.service.UserGoogleLoginError
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authServices: AuthServices
) {

    @PostMapping("/login") // login com tokens
    fun loginUser(
        @RequestBody input: UserLoginDto
    ): ResponseEntity<*> {
        val result = authServices.login(input.username, input.password)
        return when (result) {
            is Success ->
                // in the future handle cookie ...
                ResponseEntity.status(200)
                    .body(result.value)

            is Failure ->
                ResponseEntity.badRequest().build<Unit>() // still need to handle all error responses
        }
    }

    // login com o github
    @PostMapping("/logout")
    fun logoutUser() {
    }

    @GetMapping("/login/google")
    fun user(
        @AuthenticationPrincipal principal: OidcUser
    ): ResponseEntity<*> {
        val result = authServices.storeGoogleUser(
            username = principal.fullName,
            email = principal.email,
            googleId = principal.subject // googleId
        )
        return when (result) {
            is Success ->
                ResponseEntity.status(200)
                    .body(result.value)
            is Failure ->
                when (result.value) {
                    UserGoogleLoginError.InvalidCredentials-> ResponseEntity.badRequest().build<Unit>()
                }
        }
    }


}