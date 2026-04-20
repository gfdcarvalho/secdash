package com.isel.ps.secdash.service.ResponseTypes

import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.model.sast.RepositorySast
import com.isel.ps.secdash.utils.Either

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
}

typealias SastResult = Either<SastError, RepositorySast>
