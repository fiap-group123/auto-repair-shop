package br.com.autorepairshop.authentication.domain.aggregate

import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.HashedPassword
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.authentication.domain.valueobject.Role
import br.com.autorepairshop.authentication.domain.valueobject.UserId
import br.com.autorepairshop.shared.domain.AggregateRoot
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class User private constructor(
    id: UserId,
    val email: LoginEmail,
    hashedPassword: HashedPassword,
    val role: Role,
    active: Boolean,
    val customerId: UUID?,
    val createdAt: Instant,
) : AggregateRoot<UserId>(id = id) {

    var hashedPassword: HashedPassword = hashedPassword
        private set

    var active: Boolean = active
        private set

    fun deactivate() {
        if (!active) {
            throw AuthenticationException.UserInactive(message = "User is inactive.")
        }
        active = false
    }

    fun reactivate() {
        if (active) {
            throw AuthenticationException.UserAlreadyActive(message = "User is already active.")
        }
        active = true
    }

    companion object {
        fun register(
            email: LoginEmail,
            hashedPassword: HashedPassword,
            role: Role,
            customerId: UUID? = null,
            at: Instant = Clock.System.now(),
        ): User {
            if (role == Role.CLIENT && customerId == null) {
                throw AuthenticationException.InvalidRole(
                    message = "CLIENT must be linked to a customer id.",
                )
            }
            if (role != Role.CLIENT && customerId != null) {
                throw AuthenticationException.InvalidRole(
                    message = "Staff users cannot be linked to a customer id.",
                )
            }
            return User(
                id = UserId.generate(),
                email = email,
                hashedPassword = hashedPassword,
                role = role,
                active = true,
                customerId = customerId,
                createdAt = at,
            )
        }

        internal fun rehydrate(
            id: UserId,
            email: LoginEmail,
            hashedPassword: HashedPassword,
            role: Role,
            active: Boolean,
            customerId: UUID?,
            createdAt: Instant,
        ) = User(
            id = id,
            email = email,
            hashedPassword = hashedPassword,
            role = role,
            active = active,
            customerId = customerId,
            createdAt = createdAt,
        )
    }
}
