package com.isel.ps.secdash.controller.pipeline

import com.isel.ps.secdash.model.users.AuthenticatedUserDetails
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class BearerTokenAuthFilter(
    private val requestTokenProcessor: RequestTokenProcessor,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorizationHeader = request.getHeader(AuthenticationInterceptor.NAME_AUTHORIZATION_HEADER)
        val user = if (authorizationHeader != null) {
            requestTokenProcessor.processAuthorizationHeaderValue(authorizationHeader)
        } else {
            requestTokenProcessor.processCookies(request.cookies)
        }
        // When a valid token/cookie is present it is the source of truth: override any
        // SecurityContext that may have been restored from a residual HttpSession (e.g. a
        // previous OAuth2 login), otherwise hasRole(...) would evaluate the stale identity
        // while the AuthenticatedUser argument is resolved from the (correct) token.
        if (user != null) {
            val details = AuthenticatedUserDetails(user)
            val auth = UsernamePasswordAuthenticationToken(details, null, details.authorities)
            SecurityContextHolder.getContext().authentication = auth
            AuthenticatedUserArgumentResolver.addUserTo(user, request)
        }
        filterChain.doFilter(request, response)
    }
}