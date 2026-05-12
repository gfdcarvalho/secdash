package com.isel.ps.secdash.controller

import com.isel.ps.secdash.service.TeamServices
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamServices: TeamServices
) {

    //fun createTeam()
    //fun getTeam()
    //fun deleteTeam()
    //fun addUserToTeam()
    //fun removeUserFromTeam()
    //fun makeUserTeamLeader()
    //fun addRepoToTeam()
    //fun removeRepoFromTeam()
}