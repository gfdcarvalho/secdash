package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.RepositoryServices
import com.isel.ps.secdash.service.TeamServices
import com.isel.ps.secdash.service.UserDeletionError
import com.isel.ps.secdash.service.UserServices
import com.isel.ps.secdash.service.responseTypes.DeleteTeamError
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(
    private val userServices: UserServices,
    private val teamServices: TeamServices
) {

    @DeleteMapping("/delete-user/{uid}")
    @PreAuthorize("hasRole('ADMIN')") // isto funciona o user não tem que estar authenticato ?
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

}