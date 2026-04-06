package com.isel.ps.secdash.controller.pipeline

import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.model.users.TokenExternalInfo
import com.isel.ps.secdash.service.UserServices
import jakarta.servlet.http.Cookie
import kotlinx.datetime.Clock
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component


@Component
class RequestTokenProcessor(
    val userService: UserServices,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        if (parts[0].lowercase() != SCHEME) {
            return null
        }
        return userService.getUserByToken(parts[1])?.let {
            AuthenticatedUser(
                it,
                parts[1],
            )
        }
    }

    fun processCookies(cookies: Array<Cookie>?): AuthenticatedUser? {
        if (cookies == null) {
            return null
        }
        val tokenCookies = cookies.filter { it.name == TOKEN_COOKIE_NAME }
        if (tokenCookies.size != 1) {
            return null
        }
        val tokenValue = tokenCookies.single().value
        return userService.getUserByToken(tokenValue)?.let {
            AuthenticatedUser(
                it,
                tokenValue,
            )
        }
    }

    fun createCookie(tokenExternalInfo: TokenExternalInfo): ResponseCookie {
        return ResponseCookie.from(TOKEN_COOKIE_NAME, tokenExternalInfo.tokenValue)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(tokenExternalInfo.tokenExpiration.minus(Clock.System.now()).inWholeSeconds)
            .build()
    }

    fun createDeletionCookie(): ResponseCookie {
        return ResponseCookie.from(TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build()
    }

    companion object {
        const val SCHEME = "bearer"
        const val TOKEN_COOKIE_NAME = "token"
    }
}