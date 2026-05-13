package com.isel.ps.secdash.controller

import com.isel.ps.secdash.model.teams.SimpleTeamsListOutput
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.TeamServices
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamServices: TeamServices
) {


    @GetMapping
    fun getUserTeams(
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val teams = teamServices.getTeamsByUser(user.user.uid)
        return ResponseEntity.ok(SimpleTeamsListOutput(teams)) // Needs error treatment
    }

    @GetMapping("/{teamId}")
    fun getTeam(
        @PathVariable teamId: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val team = teamServices.getTeam(teamId, user.user.uid)
        return ResponseEntity.ok(team)
    }

    //fun createTeam()
    //fun getTeam()
    //fun deleteTeam()
    //fun addUserToTeam()
    //fun removeUserFromTeam()
    //fun makeUserTeamLeader()
    //fun addRepoToTeam()
    //fun removeRepoFromTeam()
}