package br.com.autorepairshop.accessidentity.application.security

import br.com.autorepairshop.accessidentity.domain.aggregate.User

interface TokenIssuer {
    fun issue(user: User): String
}
