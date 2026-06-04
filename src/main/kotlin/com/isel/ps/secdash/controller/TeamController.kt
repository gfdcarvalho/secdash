package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.teams.SimpleTeamsListOutput
import com.isel.ps.secdash.model.teams.TeamCreationInput
import com.isel.ps.secdash.model.teams.TeamRepositoryInput
import com.isel.ps.secdash.model.teams.TeamUserInput
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.AddRepositoryToTeamError
import com.isel.ps.secdash.service.AddUserToTeamError
import com.isel.ps.secdash.service.CreateTeamError
import com.isel.ps.secdash.service.DeleteTeamError
import com.isel.ps.secdash.service.GetTeamError
import com.isel.ps.secdash.service.PromoteUserToLeaderError
import com.isel.ps.secdash.service.GetTeamStatsError
import com.isel.ps.secdash.service.GetTeamSastHistoryError
import com.isel.ps.secdash.service.GetTeamSastAlertsError
import com.isel.ps.secdash.service.GetTeamVulnerabilitiesError
import com.isel.ps.secdash.service.GetTeamVulnerabilityHistoryError
import com.isel.ps.secdash.service.RemoveRepoFromTeamError
import com.isel.ps.secdash.service.RemoveUserFromTeamError
import com.isel.ps.secdash.service.TeamServices
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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

    @PostMapping("/{tid}/users")
    fun addUserToTeam(
        user: AuthenticatedUser,
        @PathVariable tid: Int,
        @RequestBody userToAdd: TeamUserInput
    ): ResponseEntity<*> {
        val result = teamServices.addUserToTeam(user.user.uid, tid, userToAdd.userId)
        return when (result) {
            is Success -> ResponseEntity.ok().build<Any>()
            is Failure ->
                when (result.value) {
                    AddUserToTeamError.OnlyTeamLeader -> Problem.response(403, Problem.onlyTeamLeader)
                    AddUserToTeamError.TeamNotFound -> Problem.response(404, Problem.teamNotFound)
                    AddUserToTeamError.UserAlreadyOnTeam -> Problem.response(409, Problem.userAlreadyOnTeam)
                    AddUserToTeamError.UserNotFound -> Problem.response(400, Problem.UserNotFound)
                }
        }
    }

    @DeleteMapping("/{tid}/users/{uidToRemove}")
    fun removeUserFromTeam(
        user: AuthenticatedUser,
        @PathVariable tid: Int,
        @PathVariable uidToRemove: Int
    ): ResponseEntity<*> {
        val result = teamServices.removeUserFromTeam(user.user.uid, tid, uidToRemove)
        return when (result) {
            is Success -> ResponseEntity.noContent().build<Any>()
            is Failure ->
                when (result.value) {
                    RemoveUserFromTeamError.TeamNotFound -> Problem.response(404, Problem.teamNotFound)
                    RemoveUserFromTeamError.OnlyTeamLeader -> Problem.response(401, Problem.onlyTeamLeader)
                    RemoveUserFromTeamError.UserNotOnTeam -> Problem.response(400 , Problem.userNotOnTeam)
                    RemoveUserFromTeamError.UserNotFound -> Problem.response(404, Problem.UserNotFound)
                }
        }
    }

    @PatchMapping("/{tid}/users/{uidToPromote}")
    fun makeUserTeamLeader(
        user: AuthenticatedUser,
        @PathVariable tid: Int,
        @PathVariable uidToPromote: Int
    ): ResponseEntity<*> {
        val result = teamServices.promoteUserToLeader(user.user.uid, tid, uidToPromote)
        return when (result) {
            is Success -> ResponseEntity.ok().build<Any>()
            is Failure ->
                when (result.value) {
                    PromoteUserToLeaderError.TeamNotFound -> Problem.response(404, Problem.teamNotFound)
                    PromoteUserToLeaderError.UserNotOnTeam -> Problem.response(400, Problem.userNotOnTeam)
                    PromoteUserToLeaderError.OnlyTeamLeader -> Problem.response(401, Problem.onlyTeamLeader)
                    PromoteUserToLeaderError.UserAlreadyLeader -> Problem.response(400, Problem.userAlreadyLeader)
                }
        }
    }

    @PostMapping("/{tid}/repository")
    fun addRepositoryToTeam(
        user: AuthenticatedUser,
        @PathVariable tid: Int,
        @RequestBody repositoryToAdd: TeamRepositoryInput
    ): ResponseEntity<*> {
        val result = teamServices.addRepositoryToTeam(user.user.uid, tid, repositoryToAdd.repositoryId)
        return when (result) {
            is Success -> ResponseEntity.ok().build<Any>()
            is Failure ->
                when (result.value) {
                    AddRepositoryToTeamError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                    AddRepositoryToTeamError.OnlyTeamLeader -> Problem.response(403, Problem.onlyTeamLeader)
                    AddRepositoryToTeamError.RepositoryAlreadyAdded -> Problem.response(409, Problem.repositoryAlreadyAdded)
                    AddRepositoryToTeamError.TeamNotFound -> Problem.response(404, Problem.teamNotFound)
                }
        }
    }

    @DeleteMapping("/{tid}/repository/{repoToRemove}")
    fun removeRepositoryFromTeam(
        user: AuthenticatedUser,
        @PathVariable tid: Int,
        @PathVariable repoToRemove: Int
    ): ResponseEntity<*> {
        val result = teamServices.removeRepositoryFromTeam(user.user.uid, tid, repoToRemove)
        return when (result) {
            is Success -> ResponseEntity.ok().build<Any>()
            is Failure ->
                when (result.value) {
                    RemoveRepoFromTeamError.TeamNotFound -> Problem.response(404, Problem.teamNotFound)
                    RemoveRepoFromTeamError.OnlyTeamLeader -> Problem.response(403, Problem.onlyTeamLeader)
                    RemoveRepoFromTeamError.RepositoryNotFound -> Problem.response(404, Problem.repositoryNotFound)
                }
        }
    }

    @GetMapping("/{tid}/stats")
    fun getTeamStats(
        @PathVariable tid: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = teamServices.getTeamStats(user.user.uid, tid)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetTeamStatsError.NotTeamMember -> Problem.response(403, Problem.forbidden)
                    GetTeamStatsError.TeamNotFound  -> Problem.response(404, Problem.teamNotFound)
                }
        }
    }



    // -------------------------- temos que garantir um scan por dia por repo para isto funcionar bem ...
    @GetMapping("/{tid}/vulnerability/history")
    fun getTeamVulnerabilityHistory(
        @PathVariable tid: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = teamServices.getTeamVulnerabilityHistory(user.user.uid, tid)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetTeamVulnerabilityHistoryError.NotTeamMember -> Problem.response(403, Problem.forbidden)
                }
        }
    }

    @GetMapping("/{tid}/sast/history")
    fun getTeamSastHistory(
        @PathVariable tid: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = teamServices.getTeamSastHistory(user.user.uid, tid)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetTeamSastHistoryError.NotTeamMember -> Problem.response(403, Problem.forbidden)
                }
        }
    }

    @GetMapping("/{tid}/vulnerabilities")
    fun getTeamVulnerabilities(
        @PathVariable tid: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = teamServices.getTeamVulnerabilities(user.user.uid, tid)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetTeamVulnerabilitiesError.NotTeamMember -> Problem.response(403, Problem.forbidden)
                }
        }
    }

    @GetMapping("/{tid}/sast")
    fun getTeamSastAlerts(
        @PathVariable tid: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = teamServices.getTeamSastAlerts(user.user.uid, tid)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetTeamSastAlertsError.NotTeamMember -> Problem.response(403, Problem.forbidden)
                }
        }
    }
}