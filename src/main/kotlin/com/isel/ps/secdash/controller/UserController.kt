package com.isel.ps.secdash.controller

import com.isel.ps.secdash.model.User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/users")
class UserController {


    @PostMapping("/register")
    fun registerUser(
        @RequestBody user: User
    ) {
    }

    @PostMapping("/login")
    fun loginUser(){ // com google relembrar como se faz !!!!

    }

    @PostMapping("/logout")
    fun logoutUser(){}

    @PostMapping("/me")
    fun me(){}

}