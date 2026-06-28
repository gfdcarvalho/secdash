package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.controller.pipeline.RequestTokenProcessor
import com.isel.ps.secdash.model.AuthProvider
import com.isel.ps.secdash.model.Platform
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.model.users.UserLoginDto
import com.isel.ps.secdash.model.users.UserOutputDto
import com.isel.ps.secdash.model.users.UserTokenOutputModel
import com.isel.ps.secdash.service.AuthServices
import com.isel.ps.secdash.service.AuthorizationError
import com.isel.ps.secdash.service.AuthorizationResult
import com.isel.ps.secdash.service.ExternalUserLoginError
import com.isel.ps.secdash.service.ExternalUserLoginResult
import com.isel.ps.secdash.service.GithubServices
import com.isel.ps.secdash.service.UserLoginError
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import com.isel.ps.secdash.controller.pipeline.SESSION_OAUTH_REDIRECT_URI
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
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

    @Value("\${frontend.url}")
    private lateinit var frontendUrl: String

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
                when (result.value) {
                    UserLoginError.InvalidCredentials -> Problem.response(400, Problem.invalidCredentials)
                }
        }
    }

    @PostMapping("/logout")
    fun logoutUser(user: AuthenticatedUser, request: HttpServletRequest): ResponseEntity<Unit> {
        authServices.logout(user.token)
        // Invalidate any HttpSession (e.g. left over from an OAuth2 login) so a stale
        // SecurityContext can't keep authenticating future requests after logout.
        request.getSession(false)?.invalidate()
        SecurityContextHolder.clearContext()
        val deletionCookie = requestTokenProcessor.createDeletionCookie()
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deletionCookie.toString())
            .build()
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
            is Success -> {
                val responseCookie = requestTokenProcessor.createCookie(result.value)
                ResponseEntity.status(302)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .header(HttpHeaders.LOCATION, "$frontendUrl/") // had to add this for the oauth process to work
//                    .build<Unit>()
                    .body(UserTokenOutputModel(result.value.token))
            }
            is Failure ->
                when (result.value) {
                    ExternalUserLoginError.InvalidCredentials -> Problem.response(400, Problem.invalidCredentials)
                }
        }
    }

    @GetMapping("/authorize/github")
    fun githubAuthorize(
        @AuthenticationPrincipal principal: OAuth2User,
        @RegisteredOAuth2AuthorizedClient("github-api") authorizedClient: OAuth2AuthorizedClient,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        val user = requestTokenProcessor.processCookies(request.cookies)
            ?: return Problem.response(401, Problem.unauthorized)
        val accessToken = authorizedClient.accessToken?.tokenValue
            ?: return Problem.response(401, Problem.unauthorized)
        val result = authServices.storeUserAuthorization(
            userId = user.user.uid,
            authProvider = AuthProvider.GITHUB,
            accessToken = accessToken,
            refreshToken = null,     // GitHub OAuth App tokens não expiram
            expiresAt = null,
        )
        return handleAuthorizationResponse(user, result, Platform.GITHUB, request, response)
    }

    @GetMapping("/authorize/gitlab")
    fun gitlabAuthorize(
        @AuthenticationPrincipal principal: OAuth2User?,
        @RegisteredOAuth2AuthorizedClient("gitlab-api") authorizedClient: OAuth2AuthorizedClient?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        val user = requestTokenProcessor.processCookies(request.cookies)
            ?: return Problem.response(401, Problem.unauthorized)
        val accessToken = authorizedClient?.accessToken?.tokenValue
            ?: return Problem.response(401, Problem.unauthorized)
        val result = authServices.storeUserAuthorization(
            userId = user.user.uid,
            authProvider = AuthProvider.GITLAB,
            accessToken = accessToken,
            refreshToken = authorizedClient.refreshToken?.tokenValue,
            expiresAt = authorizedClient.accessToken?.expiresAt,
        )
        return handleAuthorizationResponse(user, result, Platform.GITLAB, request, response)
    }

    private fun handleAuthorizationResponse(
        user: AuthenticatedUser,
        result: AuthorizationResult,
        provider: Platform,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        return when (result) {
            is Success -> {
                val redirectUri = request.cookies
                    ?.firstOrNull { it.name == SESSION_OAUTH_REDIRECT_URI }
                    ?.value
                    ?: "$frontendUrl/repos" //fallback se por algum motivo a cookie não funcionar
                val clearCookie = Cookie(SESSION_OAUTH_REDIRECT_URI, "").apply {
                    path = "/"
                    isHttpOnly = true
                    maxAge = 0
                }
                response.addCookie(clearCookie)
                ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, redirectUri)
                    .build<Unit>()
            }
            is Failure -> when (result.value) {
                AuthorizationError.InvalidToken -> Problem.response(400, Problem.invalidRequest)
                AuthorizationError.Unknown -> Problem.response(500, Problem.internalServerError)
            }
        }
    }

}