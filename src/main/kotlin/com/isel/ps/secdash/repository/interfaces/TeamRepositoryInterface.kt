package com.isel.ps.secdash.repository.interfaces

import com.isel.ps.secdash.model.teams.Team

interface TeamRepositoryInterface {
    fun findAllByUser(uid: Int): List<Team>
}