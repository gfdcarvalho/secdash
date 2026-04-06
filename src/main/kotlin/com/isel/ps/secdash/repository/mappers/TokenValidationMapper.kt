package com.isel.ps.secdash.repository.mappers

import com.isel.ps.secdash.model.users.TokenInfo
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class TokenValidationMapper : ColumnMapper<TokenInfo> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): TokenInfo = TokenInfo(r.getString(columnNumber))
}