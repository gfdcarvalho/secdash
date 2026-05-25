package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.GetRepositoryError
import com.isel.ps.secdash.service.RepositoryServices
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import com.isel.ps.secdash.utils.failure
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/repos")
class RepoController(
    private val repoServices: RepositoryServices
) {

    @GetMapping("/{repoId}")
    fun getRepo(
        @PathVariable repoId: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val result = repoServices.getRepositoryById(repoId, user.user.uid)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetRepositoryError.Unauthorized -> Problem.response(401, Problem.unauthorized)
                    GetRepositoryError.NotFound -> Problem.response(404, Problem.notFound)
                }

        }
    }
}