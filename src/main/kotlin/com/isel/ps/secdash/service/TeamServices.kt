package com.isel.ps.secdash.service

import com.isel.ps.secdash.model.users.UserDomain
import com.isel.ps.secdash.repository.interfaces.TransactionManager
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
class TeamServices(
    private val transactionManager: TransactionManager
) {

}