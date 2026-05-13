package com.isel.ps.secdash.utils

import com.isel.ps.secdash.controller.pipeline.BearerTokenAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val bearerTokenAuthFilter: BearerTokenAuthFilter,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .addFilterBefore(bearerTokenAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth
//                    .anyRequest().permitAll()
                    .requestMatchers("/auth/**", "/users/register").permitAll()
                    .anyRequest().authenticated()
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
                "github-api" -> "/auth/authorize/github"
                "google" -> "/auth/login/google"
                "gitlab" -> "/auth/login/gitlab"
                "gitlab-api" -> "/auth/authorize/gitlab"
                else -> "/"
            }
            response.sendRedirect(redirectUrl)
        }
    }
}