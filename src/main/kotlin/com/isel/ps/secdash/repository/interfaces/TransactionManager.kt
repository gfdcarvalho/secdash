package com.isel.ps.secdash.repository.interfaces



interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}