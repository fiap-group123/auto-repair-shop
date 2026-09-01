package br.com.autorepairshop.accessidentity.application.security

import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.valueobject.Role
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccessGuard(private val actors: ActorProvider) {

    fun requireCustomer(customerId: UUID) {
        val actor = actors.current()
            ?: throw AuthenticationException.Unauthenticated(message = "Authentication required.")
        if (actor.role == Role.CLIENT && actor.customerId != customerId) {
            throw AuthenticationException.Forbidden(message = "Cannot access another customer.")
        }
    }
}
