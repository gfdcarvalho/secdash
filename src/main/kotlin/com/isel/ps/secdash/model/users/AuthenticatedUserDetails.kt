package com.isel.ps.secdash.model.users

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AuthenticatedUserDetails(val authenticatedUser: AuthenticatedUser) : UserDetails {
    override fun getAuthorities() = listOf(
        SimpleGrantedAuthority("ROLE_${authenticatedUser.user.role.name}")
    )
    override fun getPassword() = null
    override fun getUsername() = authenticatedUser.user.email
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}
