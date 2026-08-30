package br.com.autorepairshop.api.security

import br.com.autorepairshop.api.dto.authentication.AuthenticatedUser
import br.com.autorepairshop.authentication.application.security.Actor
import br.com.autorepairshop.authentication.application.security.ActorProvider
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
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
