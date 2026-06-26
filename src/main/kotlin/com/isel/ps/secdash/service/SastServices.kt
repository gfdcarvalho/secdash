package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.sast.SastAlertDetail
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import com.isel.ps.secdash.utils.Either
import com.isel.ps.secdash.utils.failure
import com.isel.ps.secdash.utils.success
import org.springframework.stereotype.Service

sealed class GetSastAlertError {
    data object NotFound : GetSastAlertError()
    data object Forbidden : GetSastAlertError()
}

typealias GetSastAlertResult = Either<GetSastAlertError, SastAlertDetail>

@Service
class SastServices(
    private val transactionManager: TransactionManager,
) {
    fun getSastAlert(uid: Int, sid: Int): GetSastAlertResult {
        return transactionManager.run {
            val reposRepo = it.repositoriesRepository

            val detail = reposRepo.getSastAlertDetail(sid) ?: return@run failure(GetSastAlertError.NotFound)

            if (!reposRepo.userHasAccessToSastAlert(uid, sid)) return@run failure(GetSastAlertError.Forbidden)

            success(detail)
        }
    }
}