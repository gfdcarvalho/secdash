package com.isel.ps.secdash.controller.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI,
    detail: String? = null,
){
    val type = typeUri.toASCIIString()
    val detail = detail

    companion object {
        const val MEDIA_TYPE = "application/problem+json"

        fun response(
            status: Int,
            problem: Problem,
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        val internalServerError =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Internal Server Error",
            )

        // ------------------ User Errors
        val userAlreadyExists =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "User Already Exists",
            )
        val invalidCredentials =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Password or username is invalid",
            )
        val invalidUsername =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Username is invalid",
            )
        val invalidEmail =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Email is invalid",
            )
        val invalidPassword =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Password is invalid",
            )

        // ------------------ Auth Errors
        // using invalidCredentials from user erros for responses
        val nameIsRequired =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Username is required",
            )

        // ------------------ github / gitlab / repositories erros
        val invalidRequest =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Invalid Request",
            )
        val invalidExternalId =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "invalid External ID",
            )
        val externalIdIsRequired =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "external ID is required",
            )
        val repositoryNotFound =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Repository not found",
            )
        val userAuthorizationRequired =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "User Authorization Required",
            )
        val repositoryAlreadyAdded =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Repository already added",
            )
        val unauthorized =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "User Not Authorized",
            )
        val notFound =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Resource Not Found",
            )
        val UserNotFound =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "User Not Found",
            )
        val ownerIsRequired =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Repository Owner is Required",
            )
        val ownerNotFound =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Owner Not Found",
            )

        // ---------------- teams
        val invalidTeamName =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Invalid Team name",
            )
        val onlyTeamLeader =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Only The Team Leader can perform this action",
            )
        val teamNotFound =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "Team Not Found",
            )
        val userAlreadyOnTeam =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "User Already On Team",
            )
        val userNotOnTeam =
            Problem(
                typeUri = URI("https://github.com/gfdcarvalho/secdash/tree/master/docs/problems/example-problem"),
                detail = "User is not On Team",
            )
    }
}