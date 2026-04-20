package com.isel.ps.secdash.controller

import com.isel.ps.secdash.model.repositories.RepositoryCreationDto
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.GitlabServices
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

    @GetMapping("/repositories/{owner}")
    fun getRepositories(
        @PathVariable owner: String,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
            val result = gitlabServices.getRepositoriesByOwner(owner)
            return ResponseEntity.status(200)
                .body(result)
    }

    @PostMapping("/repositories")
    fun addRepository(
        user: AuthenticatedUser,
        @RequestBody repo: RepositoryCreationDto,
    ): ResponseEntity<*> {
        val result = gitlabServices.addRepository(repo, user.user.uid)
        return ResponseEntity.status(201)
            .body(result)
    }


    @GetMapping("/repositories/{rid}/dependency-scanning")
    fun getVulnerabilities(
        user: AuthenticatedUser,
        @PathVariable rid: Int
    ): ResponseEntity<*> {
        val result = gitlabServices.getDependencyScan(user.user.uid, rid)
        return ResponseEntity.status(200)
            .body(result)
    }

    @GetMapping("/repositories/{rid}/sast")
    fun getSast(
        user: AuthenticatedUser,
        @PathVariable rid: Int,
    ): ResponseEntity<*> {
        val result = gitlabServices.getSast(user.user.uid, rid)
        return ResponseEntity.status(200)
            .body(result)
    }


}