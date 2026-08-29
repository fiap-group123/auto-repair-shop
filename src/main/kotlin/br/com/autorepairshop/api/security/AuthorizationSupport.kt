package br.com.autorepairshop.api.security

import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.Role
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AuthorizationSupport(private val currentUser: CurrentUser) {

    fun requireCanAccessCustomer(customerId: UUID) {
        val user = currentUser.get()
        if (user.role == Role.CLIENT && user.customerId != customerId) {
            throw AuthenticationException.Forbidden(message = "Cannot access another customer.")
        }
    }

    fun requireCanAccessVehicleOwner(ownerId: UUID) {
        requireCanAccessCustomer(customerId = ownerId)
    }

    fun requireCanAccessServiceOrder(customerId: UUID) {
        requireCanAccessCustomer(customerId = customerId)
    }
}
