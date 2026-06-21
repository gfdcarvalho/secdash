package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.service.RepositoryServices
import com.isel.ps.secdash.service.UserDeletionError
import com.isel.ps.secdash.service.UserServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(
    private val userServices: UserServices
) {

    @DeleteMapping("/delete/{uid}")
    @PreAuthorize("hasRole('ADMIN')") // isto funciona o user não tem que estar authenticato ?
    fun deleteUser(
        @PathVariable uid: Int
    ): ResponseEntity<*> {
        return when (val result = userServices.deleteUser(uid)) {
            is Success -> ResponseEntity.noContent().build<Unit>()
            is Failure -> when (result.value) {
                UserDeletionError.UserNotFound -> Problem.response(404, Problem.UserNotFound)
                UserDeletionError.Forbidden -> Problem.response(403, Problem.forbidden)
            }
        }
    }

}