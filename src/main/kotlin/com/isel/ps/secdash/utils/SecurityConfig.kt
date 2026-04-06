package com.isel.ps.secdash.utils

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/", "/login", "/error", "/users/**", "/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { }   // Google login enabled

        return http.build()
    }
}