package com.isel.ps.secdash.controller

import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.GithubServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/github")
class GithubController(
    private val githubServices: GithubServices
) {


//    @GetMapping("/login")
//    fun loginUser(){}

    @GetMapping("/repos/{owner}")
    fun getRepositories(
        @PathVariable owner: String
    ): ResponseEntity<*> {
        println(owner)
        val result = githubServices.getRepositoriesByOwner(owner)
        return ResponseEntity.status(200)
            .body(result)
        /* return when (result) {
            is Success ->
                ResponseEntity.status(200)
                    .body(result.value)
            is Failure ->
                ResponseEntity.badRequest().build<Unit>()
                //when (result.value) {

                //UserGoogleLoginError.InvalidCredentials-> ResponseEntity.badRequest().build<Unit>()
                }
        }
        */
    }

    @PostMapping("/repositories")
    fun addRepository(
        user: AuthenticatedUser,
        @RequestBody repo: RepositoryCreationDto,
    ): ResponseEntity<*> {
        val result = githubServices.addRepository(repo, user.user.uid)
        return ResponseEntity.status(201)
            .body(result)
    }


    @GetMapping("/repositories/{rid}/dependabot")
    fun getDependabot(
        user: AuthenticatedUser,
        @PathVariable rid: Int
    ): ResponseEntity<*> {
        val result = githubServices.getDependabot(user.user.uid, rid)
        return ResponseEntity.status(200)
            .body(result)
    }

    @GetMapping("/sast")
    fun getSast() {}



}