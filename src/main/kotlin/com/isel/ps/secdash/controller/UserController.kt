package com.isel.ps.secdash.controller


import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.model.users.UserCreationModel
import com.isel.ps.secdash.model.users.UserCreationOutputDto
import com.isel.ps.secdash.service.UserCreationError
import com.isel.ps.secdash.service.UserServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/users")
class UserController(
    private val userServices: UserServices
) {

    @PostMapping("/register")
    fun registerUser(
        @RequestBody user: UserCreationModel
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
                    UserCreationError.UserAlreadyExists -> Problem.response(400, Problem.userAlreadyExists)
                    UserCreationError.InvalidCredentials -> Problem.response(400, Problem.invalidCredentials)
                }

        }
    }

    @GetMapping("/me")
    fun me(
        user: AuthenticatedUser,
    ) {
        println("user: ${user.user} ${user.token}")
    }
}