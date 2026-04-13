package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.pipeline.RequestTokenProcessor
import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.users.UserLoginDto
import com.isel.ps.secdash.model.users.UserTokenOutputModel
import com.isel.ps.secdash.service.AuthServices
import com.isel.ps.secdash.service.ExternalUserLoginError
import com.isel.ps.secdash.service.ExternalUserLoginResult
import com.isel.ps.secdash.service.GithubServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
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
    private val githubServices: GithubServices,
    private val requestTokenProcessor: RequestTokenProcessor
) {

    @PostMapping("/login") // login com tokens
    fun loginUser(
        @RequestBody input: UserLoginDto
    ): ResponseEntity<*> {
        val result = authServices.login(input.username, input.password)
        return when (result) {
            is Success -> {
                val responseCookie = requestTokenProcessor.createCookie(result.value)
                ResponseEntity.status(200)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(UserTokenOutputModel(result.value.token))
            }
            is Failure ->
                ResponseEntity.badRequest().build<Unit>() // still need to handle all error responses
        }
    }

    // login com o GitHub
    @PostMapping("/logout")
    fun logoutUser() {
    }

    @GetMapping("/login/google")
    fun googleLogin(
        @AuthenticationPrincipal principal: OidcUser
    ): ResponseEntity<*> {
        val result = authServices.storeExternalLoginUser(
            username = principal.fullName,
            email = principal.email,
            externalId = principal.subject, // googleId
            authProvider = AuthProvider.GOOGLE
        )
        return handleExternalLoginResponse(result)
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
            ?: githubServices.fetchGithubEmail(accessToken)
            ?: return ResponseEntity.badRequest().build<Unit>()
        val username = principal.getAttribute<String>("login")
            ?: return ResponseEntity.badRequest().build<Unit>()

        val result = authServices.storeExternalLoginUser(
            username = username,
            email = email,
            externalId = githubId,
            authProvider = AuthProvider.GITHUB
        )
        return handleExternalLoginResponse(result)
    }

    @GetMapping("/login/gitlab")
    fun gitlabLogin(
        @AuthenticationPrincipal principal: OAuth2User,
        @RegisteredOAuth2AuthorizedClient("gitlab") authorizedClient: OAuth2AuthorizedClient,
    ): ResponseEntity<*> {
        val accessToken = authorizedClient.accessToken.tokenValue
        val gitlabId = principal.getAttribute<Int>("id")?.toString()
            ?: return ResponseEntity.badRequest().build<Unit>()
        val email = principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().build<Unit>()
        val username = principal.getAttribute<String>("username")
            ?: return ResponseEntity.badRequest().build<Unit>()

        val result = authServices.storeExternalLoginUser(
            username = username,
            email = email,
            externalId = gitlabId,
            authProvider = AuthProvider.GITLAB
        )
        return handleExternalLoginResponse(result)
    }

    private fun handleExternalLoginResponse(result: ExternalUserLoginResult): ResponseEntity<*> {
        return when (result) {
            is Success ->{
                val responseCookie = requestTokenProcessor.createCookie(result.value)
                ResponseEntity.status(200)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(result.value)
            }
            is Failure ->
                when (result.value) {
                    ExternalUserLoginError.InvalidCredentials-> ResponseEntity.badRequest().build<Unit>()
                }
        }
    }

    @GetMapping("/authorize/github")
    fun githubAuthorize(
        @AuthenticationPrincipal principal: OAuth2User,
        @RegisteredOAuth2AuthorizedClient("github-api") authorizedClient: OAuth2AuthorizedClient,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val user = requestTokenProcessor.processCookies(request.cookies)
            ?: return ResponseEntity.status(401).build<Unit>() // precisamos de resolver o tratamento de erros
        val accessToken = authorizedClient.accessToken.tokenValue
        authServices.storeUserAuthorization(user.user.uid, AuthProvider.GITHUB, accessToken)
        return ResponseEntity.status(200).body(user.user) // just for testing ( password validation is in user)
    }

    @GetMapping("/authorize/gitlab")
    fun gitlabAuthorize(
        @AuthenticationPrincipal principal: OAuth2User,
        @RegisteredOAuth2AuthorizedClient("gitlab-api") authorizedClient: OAuth2AuthorizedClient,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val user = requestTokenProcessor.processCookies(request.cookies)
            ?: return ResponseEntity.status(401).build<Unit>()
        val accessToken = authorizedClient.accessToken.tokenValue
        authServices.storeUserAuthorization(user.user.uid, AuthProvider.GITLAB, accessToken)
        return ResponseEntity.status(200).body(user.user)
    }

}