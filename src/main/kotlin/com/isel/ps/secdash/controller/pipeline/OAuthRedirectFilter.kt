package com.isel.ps.secdash.controller.pipeline

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

const val SESSION_OAUTH_REDIRECT_URI = "oauth_redirect_uri"

@Component
class OAuthRedirectFilter : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith("/oauth2/authorization/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val redirectUri = request.getParameter("redirect_uri")
        if (redirectUri != null) {
            request.session.setAttribute(SESSION_OAUTH_REDIRECT_URI, redirectUri)
        }
        filterChain.doFilter(request, response)
    }
}