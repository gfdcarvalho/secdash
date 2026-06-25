package com.isel.ps.secdash.service.responseTypes

import com.isel.ps.secdash.model.repositories.ExternalRepository
import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.sast.RepositorySast
import com.isel.ps.secdash.utils.Either

sealed class GetRepositoriesError {
    data object UserAuthorizationIsRequired : GetRepositoriesError()
    data object RepositoryNotFound : GetRepositoriesError()
    data object Unauthorized : GetRepositoriesError()
}

typealias GetRepositoriesResult = Either<GetRepositoriesError, List<ExternalRepository>>

sealed class GetRepositoriesByOwnerError {
    data object Unauthorized : GetRepositoriesByOwnerError()
    data object OwnerIsRequired : GetRepositoriesByOwnerError()
    data object OwnerNotFound : GetRepositoriesByOwnerError()
}

typealias GetRepositoriesByOwnerResult = Either<GetRepositoriesByOwnerError, List<ExternalRepository>>

sealed class AddRepositoryError {
    data object NameIsRequired : AddRepositoryError()
    data object InvalidExternalId : AddRepositoryError()
    data object ExternalIdIsRequired : AddRepositoryError()
    data object RepositoryNotFound : AddRepositoryError()
    data object UserAuthorizationRequired : AddRepositoryError() //
    data object RepositoryAlreadyAdded : AddRepositoryError()
}

typealias AddRepositoryResult = Either<AddRepositoryError, Repository> // not sure what to return here ?

sealed class SastError {
    data object Unauthorized : SastError()
    data object NotFound : SastError()
    data object RepositoryNotFound : SastError()
    data object UserAuthorizationIsRequired : SastError()
    data object RepoDoesNotHaveSastFeatureEnabled : SastError()
}

typealias SastResult = Either<SastError, RepositorySast>


sealed class GetRepoByLinkError {
    data object Unauthorized : GetRepoByLinkError()
    data object RepositoryNotFound : GetRepoByLinkError()
}

typealias GetRepoByLinkResult = Either<GetRepoByLinkError, ExternalRepository>