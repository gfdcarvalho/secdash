package com.isel.ps.secdash.controller


import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.model.users.UserCreationModel
import com.isel.ps.secdash.model.users.UserCreationOutputDto
import com.isel.ps.secdash.model.users.UserProfileInformation
import com.isel.ps.secdash.service.RepositoryServices
import com.isel.ps.secdash.service.TeamServices
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
    private val userServices: UserServices,
    private val repositoryServices: RepositoryServices,
) {

//    @PreAuthorize("hasRole('ADMIN')") // so exemplo ...
    @PostMapping("/register")
    fun registerUser(
        @RequestBody user: UserCreationModel
    ): ResponseEntity<*> {
        val result = userServices.createUser(
            user.username,
            user.email,
            user.password
        )
        return when (result) {
            is Success ->
                ResponseEntity.created(URI.create("/users/me"))             // still need to think about this !
                    .body(UserCreationOutputDto(result.value))

            is Failure ->
                when (result.value) {
                    UserCreationError.UserAlreadyExists -> Problem.response(400, Problem.userAlreadyExists)
                    UserCreationError.InvalidCredentials -> Problem.response(400, Problem.invalidCredentials)
                    UserCreationError.InvalidEmail -> Problem.response(400, Problem.invalidEmail)
                    UserCreationError.InvalidPassword -> Problem.response(400, Problem.invalidPassword)
                    UserCreationError.InvalidUsername -> Problem.response(400, Problem.invalidUsername)
                }

        }
    }

    @GetMapping("/me")
    fun me(
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val repos: List<Repository> = repositoryServices.findAllByUser(user.user.uid)
        return ResponseEntity.ok(
            UserProfileInformation(
                user.user.uid,
                user.user.name,
                user.user.email,
                user.user.role,
                repos
            )
        ) // Needs error treatment
    }

}