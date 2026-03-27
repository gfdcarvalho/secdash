package com.isel.ps.secdash.controller

import com.isel.ps.secdash.model.users.User
import com.isel.ps.secdash.model.users.UserCreationDto
import com.isel.ps.secdash.model.users.UserCreationOutputDto
import com.isel.ps.secdash.service.UserCreationError
import com.isel.ps.secdash.service.UserServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import com.isel.ps.secdash.utils.success
import com.sun.jndi.toolkit.url.Uri
import org.springframework.http.ResponseEntity
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
        val result = userServices.createUser(user.username, user.email, user.password) // we could change the services to receive the Dto ??
        return when (result) {
            is Success ->
                ResponseEntity.created(URI.create("/users/me"))             // still need to think about this !
                    .body(UserCreationOutputDto(result.value))

            is Failure -> when (result.value){  // Need a better solutions for the error responses
                UserCreationError.UserAlreadyExists -> ResponseEntity.badRequest()
                UserCreationError.InvalidCredentials -> ResponseEntity.badRequest()
            }

        } as ResponseEntity<*>
    }

    @PostMapping("/login")
    fun loginUser(){ // com google relembrar como se faz !!!!

    }

    @PostMapping("/logout")
    fun logoutUser(){}

    @PostMapping("/me")
    fun me(){}

}