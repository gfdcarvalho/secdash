package com.isel.ps.secdash.utils

import com.isel.ps.secdash.controller.pipeline.BearerTokenAuthFilter
import com.isel.ps.secdash.controller.pipeline.OAuthRedirectFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val bearerTokenAuthFilter: BearerTokenAuthFilter,
    private val oAuthRedirectFilter: OAuthRedirectFilter,
    @Value("\${frontend.url}") private val frontendUrl: String,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }
            .addFilterBefore(oAuthRedirectFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(bearerTokenAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth
//                    .anyRequest().permitAll()
                    .requestMatchers("/auth/**", "/users/register", "/error").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                }
            }
            .oauth2Login { oauth2 ->
                oauth2.successHandler(oauthSuccessHandler())
                oauth2.failureHandler { _, response, _ ->
                    response.sendRedirect(frontendUrl)
                }
            }

        return http.build()
    }


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf( frontendUrl)
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
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