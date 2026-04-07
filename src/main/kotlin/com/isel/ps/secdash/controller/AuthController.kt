package com.isel.ps.secdash.controller

import com.isel.ps.secdash.model.users.UserLoginDto
import com.isel.ps.secdash.restclient.GithubRestClient
import com.isel.ps.secdash.service.AuthServices
import com.isel.ps.secdash.service.UserGithubLoginError
import com.isel.ps.secdash.service.UserGoogleLoginError
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authServices: AuthServices,
    private val githubRestClient: GithubRestClient
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
    fun googleLogin(
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

    @GetMapping("/login/github")
    fun githubLogin(
        @AuthenticationPrincipal principal: OAuth2User,
        @RegisteredOAuth2AuthorizedClient("github") authorizedClient: OAuth2AuthorizedClient
    ): ResponseEntity<*> {
        val accessToken = authorizedClient.accessToken.tokenValue
        val githubId = principal.getAttribute<Int>("id")?.toString()
            ?: return ResponseEntity.badRequest().build<Unit>()
        val email = principal.getAttribute<String>("email")
            ?: githubRestClient.fetchGithubEmail(accessToken)
            ?: return ResponseEntity.badRequest().build<Unit>()
        val username = principal.getAttribute<String>("login")
            ?: return ResponseEntity.badRequest().build<Unit>()

        val result = authServices.storeGithubUser(
            username = username,
            email = email,
            githubId = githubId,
            accessToken = accessToken,
        )
        return when (result) {
            is Success ->
                ResponseEntity.status(200).body(result.value)
            is Failure ->
                when (result.value) {
                    UserGithubLoginError.InvalidCredentials -> ResponseEntity.badRequest().build<Unit>()
                }
        }
    }


}