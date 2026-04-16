package com.isel.ps.secdash.service.ResponseTypes

import com.isel.ps.secdash.model.repositories.Repository
import com.isel.ps.secdash.utils.Either

sealed class AddRepositoryError {
    data object NameIsRequired : AddRepositoryError()
    data object InvalidExternalId : AddRepositoryError()
    data object ExternalIdIsRequired : AddRepositoryError()
    data object RepositoryNotFound : AddRepositoryError()
    data object UserAuthorizationRequired : AddRepositoryError() //
}

typealias AddRepositoryResult = Either<AddRepositoryError, Repository> // not sure what to return here ?
