package com.isel.ps.secdash.controller


import com.isel.ps.secdash.model.users.User
import com.isel.ps.secdash.model.users.UserCreationDto
import com.isel.ps.secdash.model.users.UserCreationOutputDto
import com.isel.ps.secdash.model.users.UserLoginDto
import com.isel.ps.secdash.service.UserCreationError
import com.isel.ps.secdash.service.UserGoogleLoginError
import com.isel.ps.secdash.service.UserGoogleLoginResult
import com.isel.ps.secdash.service.UserLoginError
import com.isel.ps.secdash.service.UserServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userServices: UserServices
) {


    @PostMapping("/register")
    fun registerUser(
        @RequestBody user: UserCreationDto
    ): ResponseEntity<*> {
        val result = userServices.createUser(
            user.username,
            user.email,
            user.password
        ) // we could change the services to receive the Dto ??
        return when (result) {
            is Success ->
                ResponseEntity.created(URI.create("/users/me"))             // still need to think about this !
                    .body(UserCreationOutputDto(result.value))

            is Failure ->
                when (result.value) {  // Need a better solutions for the error responses
                    UserCreationError.UserAlreadyExists -> ResponseEntity.badRequest().build<Unit>()
                    UserCreationError.InvalidCredentials -> ResponseEntity.badRequest().build<Unit>()
                }

        }
    }

    @PostMapping("/login") // login com tokens
    fun loginUser(
        @RequestBody input: UserLoginDto
    ): ResponseEntity<*> {
        val result = userServices.login(input.username, input.password)
        return when (result) {
            is Success ->
                // in the future handle cookie ...
                ResponseEntity.status(200)
                    .body(result.value)

            is Failure ->
                ResponseEntity.badRequest().build<Unit>() // still need to handle all error responses
        }
    }

    // login com o github
    @PostMapping("/logout")
    fun logoutUser() {
    }

    @PostMapping("/me")
    fun me() {
    }

    @GetMapping("/user")
    fun user(
        @AuthenticationPrincipal principal: OidcUser
    ): ResponseEntity<*> {
        val result = userServices.storeGoogleUser(
            username = principal.fullName,
            email = principal.email,
            googleId = principal.subject // googleId
        )
        return when (result) {
            is Success ->
                ResponseEntity.status(200)
                    .body(result.value)
            is Failure ->
                when (result.value) {
                    UserGoogleLoginError.InvalidCredentials-> ResponseEntity.badRequest().build<Unit>()
                }
        }
    }

}
/*

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

   @GetMapping("/")
   fun home(): String {
       return "Hello, go to /user to login"
   }

   @GetMapping("/user")
   fun user(@AuthenticationPrincipal principal: OAuth2User): Map<String, Any?> {
       println(principal)
       return mapOf(
           "name" to principal.attributes["name"],
           "email" to principal.attributes["email"]
       )
   }
}

 */