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
        if (SecurityContextHolder.getContext().authentication == null) {
            val authorizationHeader = request.getHeader(AuthenticationInterceptor.NAME_AUTHORIZATION_HEADER)
            val user = if (authorizationHeader != null) {
                requestTokenProcessor.processAuthorizationHeaderValue(authorizationHeader)
            } else {
                requestTokenProcessor.processCookies(request.cookies)
            }
            if (user != null) {
                val details = AuthenticatedUserDetails(user)
                val auth = UsernamePasswordAuthenticationToken(details, null, details.authorities)
                SecurityContextHolder.getContext().authentication = auth
                AuthenticatedUserArgumentResolver.addUserTo(user, request)
            }
        }
        filterChain.doFilter(request, response) // resolver isto !!! grave erros de redirect ...
    }
}