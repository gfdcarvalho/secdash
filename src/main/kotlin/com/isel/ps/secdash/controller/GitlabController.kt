package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.DependencyScanError
import com.isel.ps.secdash.service.GitlabServices
import com.isel.ps.secdash.service.ResponseTypes.AddRepositoryError
import com.isel.ps.secdash.service.ResponseTypes.GetRepositoriesByOwnerError
import com.isel.ps.secdash.service.ResponseTypes.GetRepositoriesError
import com.isel.ps.secdash.service.ResponseTypes.SastError
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
@RequestMapping("/gitlab")
class GitlabController(
    private val gitlabServices: GitlabServices
) {


//    @GetMapping("/login")
//    fun loginUser(){}

    @GetMapping("/repos")
    fun getRepositories(
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val result = gitlabServices.getRepositoriesFromAuthorizedUser(user.user.uid)
        return when (result) {
            is Success ->
                ResponseEntity.status(200)
                    .body(result.value)
            is Failure ->
                when (result.value) {
                    GetRepositoriesError.RepositoryNotFound -> Problem.response( 404, Problem.repositoryNotFound)
                    GetRepositoriesError.UserAuthorizationIsRequired -> Problem.response( 401, Problem.userAuthorizationRequired)
                    GetRepositoriesError.Unauthorized -> Problem.response(401, Problem.unauthorized)
                }
        }
    }

    @GetMapping("/repos/{owner}")
    fun getRepositoriesByOwner(
        @PathVariable owner: String,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val result = gitlabServices.getRepositoriesByOwner(owner)
        return when (result) {
            is Success -> ResponseEntity.status(200).body(result.value)
            is Failure -> {
                when (result.value) {
                    GetRepositoriesByOwnerError.OwnerIsRequired -> Problem.response( 400, Problem.ownerIsRequired)
                    GetRepositoriesByOwnerError.OwnerNotFound -> Problem.response( 404, Problem.ownerNotFound)
                    GetRepositoriesByOwnerError.Unauthorized -> Problem.response( 401, Problem.userAuthorizationRequired)
                }
            }
        }
    }

    @PostMapping("/repositories")
    fun addRepository(
        user: AuthenticatedUser,
        @RequestBody repo: RepositoryCreationDto,
    ): ResponseEntity<*> {
        val result = gitlabServices.addRepository(repo, user.user.uid)
        return when (result) {
            is Success -> ResponseEntity.status(200).body(result.value)
            is Failure -> {
                when (result.value) {
                    AddRepositoryError.RepositoryAlreadyAdded -> Problem.response(400, Problem.repositoryAlreadyAdded)
                    AddRepositoryError.ExternalIdIsRequired -> Problem.response(400, Problem.externalIdIsRequired)
                    AddRepositoryError.InvalidExternalId -> Problem.response(400, Problem.invalidExternalId)
                    AddRepositoryError.NameIsRequired -> Problem.response(400, Problem.nameIsRequired)
                    AddRepositoryError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                    AddRepositoryError.UserAuthorizationRequired -> Problem.response(401, Problem.userAuthorizationRequired)
                }
            }
        }
    }


    @GetMapping("/repositories/{rid}/dependency-scanning")
    fun getVulnerabilities(
        user: AuthenticatedUser,
        @PathVariable rid: Int
    ): ResponseEntity<*> {
        val result = gitlabServices.getDependencyScan(user.user.uid, rid)
        return when (result) {
            is Success -> ResponseEntity.status(200).body(result)
            is Failure ->
                when (result.value) {
                    DependencyScanError.NotFound -> Problem.response(404, Problem.notFound)
                    DependencyScanError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                    DependencyScanError.Unauthorized -> Problem.response( 401, Problem.unauthorized)
                }
        }
    }

    @GetMapping("/repositories/{rid}/sast")
    fun getSast(
        user: AuthenticatedUser,
        @PathVariable rid: Int,
    ): ResponseEntity<*> {
        val result = gitlabServices.getSast(user.user.uid, rid)
        return when (result) {
            is Success ->
                ResponseEntity.status(200).body(result)

            is Failure ->
                when (result.value) {
                    SastError.Unauthorized -> Problem.response(401, Problem.unauthorized)
                    SastError.NotFound -> Problem.response(404, Problem.notFound)
                    SastError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                    SastError.RepoDoesNotHaveSastFeatureEnabled -> Problem.response(403, Problem.repoDoesNotHaveDependabotFeatureEnabled)
                }
        }
    }
}