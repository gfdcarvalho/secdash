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
        private const val BASE_URI = "https://github.com/gfdcarvalho/secdash/tree/master/docs/problems"

        fun response(
            status: Int,
            problem: Problem,
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        val internalServerError =
            Problem(
                typeUri = URI("$BASE_URI/internal-server-error"),
                detail = "Internal Server Error",
            )

        // ------------------ User Errors
        val userAlreadyExists =
            Problem(
                typeUri = URI("$BASE_URI/user-already-exists"),
                detail = "User Already Exists",
            )
        val invalidCredentials =
            Problem(
                typeUri = URI("$BASE_URI/invalid-credentials"),
                detail = "Password or username is invalid",
            )
        val invalidUsername =
            Problem(
                typeUri = URI("$BASE_URI/invalid-username"),
                detail = "Username is invalid",
            )
        val invalidEmail =
            Problem(
                typeUri = URI("$BASE_URI/invalid-email"),
                detail = "Email is invalid",
            )
        val invalidPassword =
            Problem(
                typeUri = URI("$BASE_URI/invalid-password"),
                detail = "Password is invalid",
            )

        // ------------------ Auth Errors
        // using invalidCredentials from user erros for responses
        val nameIsRequired =
            Problem(
                typeUri = URI("$BASE_URI/name-is-required"),
                detail = "Username is required",
            )

        // ------------------ github / gitlab / repositories erros
        val invalidRequest =
            Problem(
                typeUri = URI("$BASE_URI/invalid-request"),
                detail = "Invalid Request",
            )
        val invalidExternalId =
            Problem(
                typeUri = URI("$BASE_URI/invalid-external-id"),
                detail = "invalid External ID",
            )
        val externalIdIsRequired =
            Problem(
                typeUri = URI("$BASE_URI/external-id-is-required"),
                detail = "external ID is required",
            )
        val repositoryNotFound =
            Problem(
                typeUri = URI("$BASE_URI/repository-not-found"),
                detail = "Repository not found",
            )
        val userAuthorizationRequired =
            Problem(
                typeUri = URI("$BASE_URI/user-authorization-required"),
                detail = "User Authorization Required",
            )
        val repositoryAlreadyAdded =
            Problem(
                typeUri = URI("$BASE_URI/repository-already-added"),
                detail = "Repository already added",
            )
        val unauthorized =
            Problem(
                typeUri = URI("$BASE_URI/unauthorized"),
                detail = "User Not Authorized",
            )
        val notFound =
            Problem(
                typeUri = URI("$BASE_URI/not-found"),
                detail = "Resource Not Found",
            )
        val UserNotFound =
            Problem(
                typeUri = URI("$BASE_URI/user-not-found"),
                detail = "User Not Found",
            )
        val ownerIsRequired =
            Problem(
                typeUri = URI("$BASE_URI/owner-is-required"),
                detail = "Repository Owner is Required",
            )
        val ownerNotFound =
            Problem(
                typeUri = URI("$BASE_URI/owner-not-found"),
                detail = "Owner Not Found",
            )
        val repoDoesNotHaveSastFeatureEnabled =
            Problem(
                typeUri = URI("$BASE_URI/repo-does-not-have-sast-feature-enabled"),
                detail = "Invalid Team name",
            )
        val repoDoesNotHaveDependabotFeatureEnabled =
            Problem(
                typeUri = URI("$BASE_URI/repo-does-not-have-dependabot-feature-enabled"),
                detail = "Invalid Team name",
            )

        // ---------------- teams
        val invalidTeamName =
            Problem(
                typeUri = URI("$BASE_URI/invalid-team-name"),
                detail = "Invalid Team name",
            )
        val onlyTeamLeader =
            Problem(
                typeUri = URI("$BASE_URI/only-team-leader"),
                detail = "Only The Team Leader can perform this action",
            )
        val teamNotFound =
            Problem(
                typeUri = URI("$BASE_URI/team-not-found"),
                detail = "Team Not Found",
            )
        val userAlreadyOnTeam =
            Problem(
                typeUri = URI("$BASE_URI/user-already-on-team"),
                detail = "User Already On Team",
            )
        val userNotOnTeam =
            Problem(
                typeUri = URI("$BASE_URI/user-not-on-team"),
                detail = "User is not On Team",
            )
        val userAlreadyLeader =
            Problem(
                typeUri = URI("$BASE_URI/user-already-leader"),
                detail = "User is Already a Team Leader",
            )

        val forbidden =
            Problem(
                typeUri = URI("$BASE_URI/forbidden"),
                detail = "User is not Authorized",
            )
    }
}