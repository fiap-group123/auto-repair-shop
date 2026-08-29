package br.com.autorepairshop.authentication.domain.aggregate

import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.HashedPassword
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.authentication.domain.valueobject.Role
import br.com.autorepairshop.authentication.domain.valueobject.UserId
import br.com.autorepairshop.shared.domain.Entity
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
    val registeredAt: Instant,
) : Entity<UserId>(id = id) {

    var hashedPassword: HashedPassword = hashedPassword
        private set

    var active: Boolean = active
        private set

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
                registeredAt = at,
            )
        }

        internal fun rehydrate(
            id: UserId,
            email: LoginEmail,
            hashedPassword: HashedPassword,
            role: Role,
            active: Boolean,
            customerId: UUID?,
            registeredAt: Instant,
        ) = User(
            id = id,
            email = email,
            hashedPassword = hashedPassword,
            role = role,
            active = active,
            customerId = customerId,
            registeredAt = registeredAt,
        )
    }
}
