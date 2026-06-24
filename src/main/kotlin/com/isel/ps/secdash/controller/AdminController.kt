package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.GetUserError
import com.isel.ps.secdash.service.TeamServices
import com.isel.ps.secdash.service.UserDeletionError
import com.isel.ps.secdash.service.UserPromotionError
import com.isel.ps.secdash.service.UserServices
import com.isel.ps.secdash.service.responseTypes.DeleteTeamError
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val userServices: UserServices,
    private val teamServices: TeamServices
) {

    @DeleteMapping("/delete-user/{uid}")
    fun deleteUser(
        @PathVariable uid: Int
    ): ResponseEntity<*> {
        return when (val result = userServices.deleteUser(uid)) {
            is Success -> ResponseEntity.noContent().build<Unit>()
            is Failure -> when (result.value) {
                UserDeletionError.UserNotFound -> Problem.response(404, Problem.UserNotFound)
                UserDeletionError.Forbidden -> Problem.response(403, Problem.forbidden)
            }
        }
    }

    @DeleteMapping("/delete-team/{teamId}")
    fun deleteTeam(
        user: AuthenticatedUser,
        @PathVariable teamId: Int
    ): ResponseEntity<*> {
        val result = teamServices.deleteTeam(user.user.uid, user.user.role, teamId)
        return when (result) {
            is Success -> ResponseEntity.noContent().build<Any>()
            is Failure ->
                when (result.value) {
                    DeleteTeamError.TeamNotFound -> Problem.response(404, Problem.teamNotFound)
                    DeleteTeamError.OnlyTeamLeaderOrAdmin -> Problem.response(401, Problem.onlyTeamLeader)
                }
        }
    }

    @PostMapping("/promote-user/{userId}")
    fun promoteUser(
        @PathVariable userId: Int
    ): ResponseEntity<*> {
        return when (val result = userServices.promoteUser(userId)) {
            is Success -> ResponseEntity.noContent().build<Unit>()
            is Failure -> when (result.value) {
                UserPromotionError.UserNotFound -> Problem.response(404, Problem.UserNotFound)
            }
        }
    }

    // used for the tests
    @GetMapping("/users/{userId}")
    fun getUser(
        user: AuthenticatedUser,
        @PathVariable userId: Int
    ): ResponseEntity<*> {
        val result = userServices.getUser(userId)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value.toOutputDto())
            is Failure -> when (result.value) {
                GetUserError.UserNotFound -> Problem.response(404, Problem.UserNotFound)
            }
        }
    }
}