package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.DeleteRepositoryError
import com.isel.ps.secdash.service.GetRepositoryError
import com.isel.ps.secdash.service.RepositoryServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
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

    @DeleteMapping("/{repoId}")
    fun deleteRepo(
        @PathVariable repoId: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val result = repoServices.deleteRepository(repoId, user.user.uid)
        return when (result) {
            is Success -> ResponseEntity.noContent().build<Any>()
            is Failure ->
                when (result.value) {
                    DeleteRepositoryError.NotFound -> Problem.response(404, Problem.notFound)
                    DeleteRepositoryError.Unauthorized -> Problem.response(401, Problem.unauthorized)
                }
        }
    }

    @GetMapping
    fun getRepos(
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = repoServices.findAllByUser(user.user.uid)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{repoId}/stats")
    fun getRepoStats(
        @PathVariable repoId: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val result = repoServices.getRepoStats(user.user.uid, repoId)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetRepositoryError.Unauthorized -> Problem.response(403, Problem.forbidden)
                    GetRepositoryError.NotFound  -> Problem.response(404, Problem.repositoryNotFound)
                }
        }
    }

    @GetMapping("/{repoId}/vulnerability/history")
    fun getRepoVulnerabilityHistory(
        @PathVariable repoId: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = repoServices.getRepoVulnerabilityHistory(user.user.uid, repoId)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetRepositoryError.Unauthorized -> Problem.response(403, Problem.forbidden)
                    GetRepositoryError.NotFound  -> Problem.response(404, Problem.repositoryNotFound)
                }
        }
    }
}