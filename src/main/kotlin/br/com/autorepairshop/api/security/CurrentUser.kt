package br.com.autorepairshop.api.security

import br.com.autorepairshop.accessidentity.application.security.Actor
import br.com.autorepairshop.accessidentity.application.security.ActorProvider
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.api.dto.accessidentity.AuthenticatedUser
import org.springframework.stereotype.Component

@Component
class CurrentUser(private val actors: ActorProvider) {
    fun get(): AuthenticatedUser {
        val actor = actors.current()
            ?: throw AuthenticationException.Unauthenticated(message = "Authentication required.")
        return actor.toAuthenticatedUser()
    }

    private fun Actor.toAuthenticatedUser() = AuthenticatedUser(
        userId = userId,
        role = role,
        customerId = customerId,
    )
}
