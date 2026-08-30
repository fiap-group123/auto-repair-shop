package br.com.autorepairshop.authentication.domain.aggregate

import br.com.autorepairshop.authentication.domain.InviteToken
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.CustomerInviteId
import br.com.autorepairshop.shared.domain.AggregateRoot
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class CustomerInvite private constructor(
    id: CustomerInviteId,
    val customerId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    consumedAt: Instant?,
    val createdAt: Instant,
) : AggregateRoot<CustomerInviteId>(id = id) {

    var consumedAt: Instant? = consumedAt
        private set

    fun requireUsable(at: Instant = Clock.System.now()) {
        if (consumedAt != null) {
            throw AuthenticationException.InviteConsumed(message = "Invite was already used.")
        }
        if (at >= expiresAt) {
            throw AuthenticationException.InviteExpired(message = "Invite has expired.")
        }
    }

    fun consume(at: Instant = Clock.System.now()) {
        requireUsable(at = at)
        consumedAt = at
    }

    fun revoke(at: Instant = Clock.System.now()) {
        if (consumedAt == null) {
            consumedAt = at
        }
    }

    companion object {
        val DEFAULT_TTL: Duration = 72.hours

        data class Issued(
            val invite: CustomerInvite,
            val rawToken: String,
        )

        fun issue(
            customerId: UUID,
            at: Instant = Clock.System.now(),
            ttl: Duration = DEFAULT_TTL,
        ): Issued {
            val rawToken = InviteToken.generate()
            val invite = CustomerInvite(
                id = CustomerInviteId.generate(),
                customerId = customerId,
                tokenHash = InviteToken.hash(raw = rawToken),
                expiresAt = at + ttl,
                consumedAt = null,
                createdAt = at,
            )
            return Issued(
                invite = invite,
                rawToken = rawToken,
            )
        }

        internal fun rehydrate(
            id: CustomerInviteId,
            customerId: UUID,
            tokenHash: String,
            expiresAt: Instant,
            consumedAt: Instant?,
            createdAt: Instant,
        ) = CustomerInvite(
            id = id,
            customerId = customerId,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            consumedAt = consumedAt,
            createdAt = createdAt,
        )
    }
}
