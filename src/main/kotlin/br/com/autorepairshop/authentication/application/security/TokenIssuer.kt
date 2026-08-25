package br.com.autorepairshop.authentication.application.security

import br.com.autorepairshop.authentication.domain.aggregate.User

interface TokenIssuer {
    fun issue(user: User): String
}
