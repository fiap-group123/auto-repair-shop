package br.com.autorepairshop.accessidentity.domain.aggregate

import br.com.autorepairshop.accessidentity.domain.SecureToken
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.valueobject.RefreshSessionId
import br.com.autorepairshop.shared.domain.AggregateRoot
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class RefreshSession private constructor(
    id: RefreshSessionId,
    val userId: UUID,
    val familyId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    revokedAt: Instant?,
    replacedBy: RefreshSessionId?,
    val createdAt: Instant,
) : AggregateRoot<RefreshSessionId>(id = id) {

    var revokedAt: Instant? = revokedAt
        private set

    var replacedBy: RefreshSessionId? = replacedBy
        private set

    fun requireUsable(at: Instant = Clock.System.now()) {
        if (revokedAt != null) {
            throw AuthenticationException.InvalidRefresh(message = "Refresh token was revoked.")
        }
        if (at >= expiresAt) {
            throw AuthenticationException.InvalidRefresh(message = "Refresh token has expired.")
        }
    }

    fun revoke(at: Instant = Clock.System.now()) {
        if (revokedAt == null) {
            revokedAt = at
        }
    }

    fun rotate(
        at: Instant = Clock.System.now(),
        ttl: Duration,
    ): Issued {
        requireUsable(at = at)
        val issued = issue(
            userId = userId,
            familyId = familyId,
            at = at,
            ttl = ttl,
        )
        revokedAt = at
        replacedBy = issued.session.id
        return issued
    }

    companion object {
        data class Issued(
            val session: RefreshSession,
            val rawToken: String,
        )

        fun issue(
            userId: UUID,
            familyId: UUID = UUID.randomUUID(),
            at: Instant = Clock.System.now(),
            ttl: Duration,
        ): Issued {
            val rawToken = SecureToken.generate()
            val session = RefreshSession(
                id = RefreshSessionId.generate(),
                userId = userId,
                familyId = familyId,
                tokenHash = SecureToken.hash(raw = rawToken),
                expiresAt = at + ttl,
                revokedAt = null,
                replacedBy = null,
                createdAt = at,
            )
            return Issued(
                session = session,
                rawToken = rawToken,
            )
        }

        internal fun rehydrate(
            id: RefreshSessionId,
            userId: UUID,
            familyId: UUID,
            tokenHash: String,
            expiresAt: Instant,
            revokedAt: Instant?,
            replacedBy: RefreshSessionId?,
            createdAt: Instant,
        ) = RefreshSession(
            id = id,
            userId = userId,
            familyId = familyId,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            revokedAt = revokedAt,
            replacedBy = replacedBy,
            createdAt = createdAt,
        )
    }
}
