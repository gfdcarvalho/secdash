package com.isel.ps.secdash.utils

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .anyRequest().permitAll()
//                    .requestMatchers("/", "/login", "/error", "/users/**", "/auth/**", "/github/repositories").permitAll()
//                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.successHandler(oauthSuccessHandler())
            }

        return http.build()
    }


    @Bean
    fun oauthSuccessHandler(): AuthenticationSuccessHandler {
        return AuthenticationSuccessHandler { _, response, authentication ->
            val token = authentication as OAuth2AuthenticationToken
            val redirectUrl = when (token.authorizedClientRegistrationId) {
                "github" -> "/auth/login/github"
                "google" -> "/auth/login/google"
                else -> "/"
            }
            response.sendRedirect(redirectUrl)
        }
    }
}

