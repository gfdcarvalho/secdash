package com.isel.ps.secdash.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/api/github")
class GithubController {


//    @GetMapping("/login")
//    fun loginUser(){}

    @GetMapping("/repositories")
    fun getRepositories()  {}

    @GetMapping("/vulnerabilities")
    fun getVulnerabilities() {}

    @GetMapping("/sast")
    fun getSast() {}



}