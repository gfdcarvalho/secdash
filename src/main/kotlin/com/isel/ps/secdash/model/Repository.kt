package com.isel.ps.secdash.model

import java.util.Date


class Repository(
    val id: Int,
    val name: String,
    val externalId: String,
    val platform: Platform,
    val owner: Owner,
    val htmlUrl: String,
    val description: String,
    val issuesCount: Int,
    val createdAt: Date,
    val updatedAt: Date,
    val forksCount: Int,
    val visibility: Visibility
    ) {
    enum class Visibility {
        PUBLIC, PRIVATE, INTERNAL
    }
}

/*
temos que adicionar um campo para o id do repo (id dado pelo github e pelo gitlab)
porque para futuras chamadas a estes repositorios é esse id que deveriamos usar e não o nome do repo porque o nome pode ser alterado

sugestão do claudio !!!
class Repository(
    val id: UUID,
    val externalId: String,       // ID no GitHub/GitLab
    val name: String,
    val fullName: String,         // ex: "RodrigoVitorino/SecDash-test"
    val platform: Platform,
    val owner: Owner,
    val url: String,              // agnóstico ao provider
    val description: String?,     // nullable — pode não ter
    val defaultBranch: String,
    val visibility: Visibility,
    val lastActivityAt: Instant,  // vem do provider
    val createdAt: Instant        // vem do provider
) {
    enum class Visibility {
        PUBLIC, PRIVATE, INTERNAL
    }
}


portanto o fluxo de utilização seria:
utilizador da login com a conta github/gitlab
usando o token do utilizador mostramos todos os repos disponivies
utilizador escolhe os repos que quer que o secdash mostre
guardamos informação sobre esses repos (id,nome etc) e a partir daqui fazemos chamadas a api com esse id (imutavel)
 */