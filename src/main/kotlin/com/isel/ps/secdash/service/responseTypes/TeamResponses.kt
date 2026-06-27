package com.isel.ps.secdash.service.responseTypes

import com.isel.ps.secdash.model.sast.TeamSastAlerts
import com.isel.ps.secdash.model.teams.DailySastCount
import com.isel.ps.secdash.model.teams.DailyVulnerabilityCount
import com.isel.ps.secdash.model.teams.Team
import com.isel.ps.secdash.model.teams.TeamStats
import com.isel.ps.secdash.model.vulnerability.TeamVulnerabilities
import com.isel.ps.secdash.utils.Either

sealed class GetTeamError {
    data object Unauthorized : GetTeamError()
    data object TeamNotFound : GetTeamError()
}

typealias GetTeamResult = Either<GetTeamError, Team>

sealed class CreateTeamError {
    data object InvalidName: CreateTeamError()
    data object InternalError: CreateTeamError()
}

typealias CreateTeamResult = Either<CreateTeamError, Team>

sealed class DeleteTeamError {
    data object OnlyTeamLeaderOrAdmin: DeleteTeamError()
    data object TeamNotFound: DeleteTeamError()
}

typealias DeleteTeamResult = Either<DeleteTeamError, Unit>

sealed class AddUserToTeamError {
    data object OnlyTeamLeader: AddUserToTeamError()
    data object TeamNotFound: AddUserToTeamError()
    data object UserNotFound: AddUserToTeamError()
    data object UserAlreadyOnTeam: AddUserToTeamError()
}

typealias AddUserToTeamResult = Either<AddUserToTeamError, Unit>

sealed class RemoveUserFromTeamError {
    data object OnlyTeamLeader: RemoveUserFromTeamError()
    data object TeamNotFound: RemoveUserFromTeamError()
    data object UserNotFound: RemoveUserFromTeamError()
    data object UserNotOnTeam: RemoveUserFromTeamError()
}

typealias RemoveUserFromTeamResult = Either<RemoveUserFromTeamError, Unit>

sealed class PromoteUserToLeaderError {
    data object OnlyTeamLeader: PromoteUserToLeaderError()
    data object TeamNotFound: PromoteUserToLeaderError()
    data object UserNotOnTeam: PromoteUserToLeaderError()
    data object UserAlreadyLeader: PromoteUserToLeaderError()
}

typealias PromoteUserToLeaderResult = Either<PromoteUserToLeaderError, Unit>

sealed class AddRepositoryToTeamError {
    data object OnlyTeamLeader: AddRepositoryToTeamError()
    data object TeamNotFound: AddRepositoryToTeamError()
    data object RepositoryNotFound: AddRepositoryToTeamError()
    data object RepositoryAlreadyAdded: AddRepositoryToTeamError()
}

typealias AddRepositoryToTeamResult = Either<AddRepositoryToTeamError, Unit>

sealed class RemoveRepoFromTeamError {
    data object OnlyTeamLeader: RemoveRepoFromTeamError()
    data object TeamNotFound: RemoveRepoFromTeamError()
    data object RepositoryNotFound: RemoveRepoFromTeamError()
}

typealias RemoveRepoFromTeamResult = Either<RemoveRepoFromTeamError, Unit>

sealed class GetTeamStatsError {
    data object NotTeamMember: GetTeamStatsError()
    data object TeamNotFound: GetTeamStatsError()
}

typealias GetTeamStatsResult = Either<GetTeamStatsError, TeamStats>

sealed class GetTeamVulnerabilityHistoryError {
    data object NotTeamMember: GetTeamVulnerabilityHistoryError()
    data object TeamNotFound: GetTeamVulnerabilityHistoryError()
}

typealias GetTeamVulnerabilityHistoryResult = Either<GetTeamVulnerabilityHistoryError, List<DailyVulnerabilityCount>>

sealed class GetTeamSastHistoryError {
    data object NotTeamMember: GetTeamSastHistoryError()
    data object TeamNotFound: GetTeamSastHistoryError()
}

typealias GetTeamSastHistoryResult = Either<GetTeamSastHistoryError, List<DailySastCount>>

sealed class GetTeamVulnerabilitiesError {
    data object NotTeamMember: GetTeamVulnerabilitiesError()
    data object TeamNotFound: GetTeamVulnerabilitiesError()
}

typealias GetTeamVulnerabilitiesResult = Either<GetTeamVulnerabilitiesError, List<TeamVulnerabilities>>

sealed class GetTeamSastAlertsError {
    data object NotTeamMember: GetTeamSastAlertsError()
    data object TeamNotFound: GetTeamSastAlertsError()
}

typealias GetTeamSastAlertsResult = Either<GetTeamSastAlertsError, List<TeamSastAlerts>>