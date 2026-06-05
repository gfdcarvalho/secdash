package com.isel.ps.secdash.controller

import com.isel.ps.secdash.controller.model.Problem
import com.isel.ps.secdash.model.users.AuthenticatedUser
import com.isel.ps.secdash.service.GetSastAlertError
import com.isel.ps.secdash.service.SastServices
import com.isel.ps.secdash.utils.Failure
import com.isel.ps.secdash.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sast")
class SastController(
    private val sastServices: SastServices,
) {
    @GetMapping("/{sid}")
    fun getSastAlert(
        @PathVariable sid: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val result = sastServices.getSastAlert(user.user.uid, sid)
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    GetSastAlertError.Forbidden -> Problem.response(403, Problem.forbidden)
                    GetSastAlertError.NotFound  -> Problem.response(404, Problem.notFound)
                }
        }
    }
}