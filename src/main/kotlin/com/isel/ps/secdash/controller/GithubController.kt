package com.isel.ps.secdash.controller

import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.GithubServices
import com.isel.ps.secdash.service.UserGoogleLoginError
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @GetMapping("/vulnerabilities")
    fun getVulnerabilities() {}

    @GetMapping("/sast")
    fun getSast() {}



}