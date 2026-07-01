package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.DependabotError
import com.isel.ps.secdash.service.GithubServices
import com.isel.ps.secdash.service.responseTypes.AddRepositoryError
import com.isel.ps.secdash.service.responseTypes.GetRepoByLinkError
import com.isel.ps.secdash.service.responseTypes.GetRepositoriesByOwnerError
import com.isel.ps.secdash.service.responseTypes.GetRepositoriesError
import com.isel.ps.secdash.service.responseTypes.SastError
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @GetMapping("/repos")
    fun getRepositories(
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = githubServices.getRepositoriesFromAuthorizedUser(user.user.uid)
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
        user: AuthenticatedUser,
        @PathVariable owner: String,
    ): ResponseEntity<*> {
        val result = githubServices.getRepositoriesByOwner(owner)
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
        val result = githubServices.addRepository(repo, user.user.uid)
        return when (result) {
            is Success ->
                ResponseEntity.status(200).body(result.value)
            is Failure ->
                when (result.value) {
                    AddRepositoryError.RepositoryAlreadyAdded -> Problem.response(400, Problem.repositoryAlreadyAdded)
                    AddRepositoryError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                    AddRepositoryError.NameIsRequired -> Problem.response(400, Problem.nameIsRequired)
                    //AddRepositoryError.InvalidExternalId -> Problem.response(400, Problem.invalidExternalId)
                    AddRepositoryError.UserAuthorizationRequired -> Problem.response(401, Problem.userAuthorizationRequired)
                    //AddRepositoryError.ExternalIdIsRequired -> Problem.response(400, Problem.externalIdIsRequired)
                    else -> Problem.response(500, Problem.internalServerError) // the 2 other error cases should never happen in this endpoint
                }
        }
    }


    @GetMapping("/repositories/{rid}/dependabot")
    fun getDependabot(
        user: AuthenticatedUser,
        @PathVariable rid: Int
    ): ResponseEntity<*> {
        val result = githubServices.getDependabot(user.user.uid, rid)
        return when (result) {
            is Success ->
                ResponseEntity.status(200).body(result.value)
            is Failure ->
                when (result.value) {
                    DependabotError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                    DependabotError.UserAuthorizationIsRequired -> Problem.response(401, Problem.userAuthorizationRequired)
                    DependabotError.NotFound -> Problem.response(404, Problem.notFound)
                    DependabotError.Unauthorized -> Problem.response(401, Problem.unauthorized)
                    DependabotError.RepoDoesNotHaveDependabotFeature -> Problem.response(403, Problem.repoDoesNotHaveDependabotFeatureEnabled)
                }
        }
    }

    @GetMapping("/repositories/{rid}/sast")
    fun getSast(
        user: AuthenticatedUser,
        @PathVariable rid: Int
    ): ResponseEntity<*> {
        val result = githubServices.getSastAlerts(user.user.uid, rid)
        return when (result) {
            is Success ->
                ResponseEntity.status(200).body(result.value)
            is Failure ->
                when (result.value) {
                    SastError.Unauthorized -> Problem.response(401, Problem.unauthorized)
                    SastError.UserAuthorizationIsRequired -> Problem.response(401, Problem.userAuthorizationRequired)
                    SastError.NotFound -> Problem.response(404, Problem.notFound)
                    SastError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                    SastError.RepoDoesNotHaveSastFeatureEnabled -> Problem.response(403, Problem.repoDoesNotHaveSastFeatureEnabled)
                }
        }
    }

    @GetMapping("/repository") // GET /github/repository?link={githubRepoUrl}
    fun getRepoByLink(
        user: AuthenticatedUser,
        @RequestParam link: String,
    ): ResponseEntity<*> {
        val result = githubServices.getRepoByLink(link)
        return when (result) {
            is Success -> ResponseEntity.status(200).body(result.value)
            is Failure ->
                when (result.value) {
                    GetRepoByLinkError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                    GetRepoByLinkError.Unauthorized -> Problem.response(401, Problem.userAuthorizationRequired)
                }
        }
    }



}