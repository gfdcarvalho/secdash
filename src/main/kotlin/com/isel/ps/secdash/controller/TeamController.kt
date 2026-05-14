package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.teams.SimpleTeamsListOutput
import com.isel.ps.secdash.model.teams.TeamCreationInput
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.CreateTeamError
import com.isel.ps.secdash.service.DeleteTeamError
import com.isel.ps.secdash.service.GetTeamError
import com.isel.ps.secdash.service.TeamServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamServices: TeamServices
) {


    @GetMapping
    fun getUserTeams(
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val result = teamServices.getTeamsByUser(user.user.uid)
        return when (result) {
            is Success -> ResponseEntity.ok(SimpleTeamsListOutput(result.value))
            is Failure -> TODO() // for now this endpoint has no errors
        }
    }

    @GetMapping("/{teamId}")
    fun getTeam(
        @PathVariable teamId: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val result = teamServices.getTeam(teamId, user.user.uid)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value){
                    GetTeamError.Unauthorized -> Problem.response(401, Problem.unauthorized)
                }
        }
    }

    @PostMapping
    fun createTeam(
        user: AuthenticatedUser,
        @RequestBody team: TeamCreationInput
    ): ResponseEntity<*> {
        val result = teamServices.createTeam(
            user.user.uid,
            team.name,
            team.description
        )
        return when (result) {
            is Success -> ResponseEntity.created(URI("/teams/${result.value.tid}")).body(result.value)
            is Failure ->
                when (result.value){
                    CreateTeamError.InvalidName -> Problem.response(400, Problem.invalidTeamName)
                    CreateTeamError.InternalError -> Problem.response(500 , Problem.internalServerError)
                }
        }

    }

    @DeleteMapping("/{teamId}")
    fun deleteTeam(
        user: AuthenticatedUser,
        @PathVariable teamId: Int
    ): ResponseEntity<*> {
        val result = teamServices.deleteTeam(user.user.uid, teamId)
        return when (result) {
            is Success -> ResponseEntity.noContent().build<Any>()
            is Failure ->
                when (result.value) {
                    DeleteTeamError.TeamNotFound -> Problem.response(400, Problem.TeamNotFound)
                    DeleteTeamError.OnlyTeamLeader -> Problem.response(401, Problem.onlyTeamLeader)
                }
        }
    }

    //fun addUserToTeam()
    //fun removeUserFromTeam()
    //fun makeUserTeamLeader()
    //fun addRepoToTeam()
    //fun removeRepoFromTeam()
}