package com.isel.ps.secdash.controller.pipeline

import com.isel.ps.secdash.controller.model.Problem
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAccessDenied(): ResponseEntity<*> =
        Problem.response(403, Problem.forbidden)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<Any> {
        logger.error("Unhandled exception: ${ex.message}", ex)
        return Problem.response(500, Problem.internalServerError)
    }
}
