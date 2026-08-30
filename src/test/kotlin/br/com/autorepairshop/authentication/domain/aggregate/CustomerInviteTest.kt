package br.com.autorepairshop.authentication.domain.aggregate

import br.com.autorepairshop.authentication.domain.InviteToken
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Tag("unit")
class CustomerInviteTest {

    @Test
    fun `issue creates a usable invite hashed from the raw token`() {
        val customerId = UUID.randomUUID()
        val issued = CustomerInvite.issue(customerId = customerId)

        assertEquals(
            expected = customerId,
            actual = issued.invite.customerId,
        )
        assertEquals(
            expected = InviteToken.hash(raw = issued.rawToken),
            actual = issued.invite.tokenHash,
        )
        assertNull(issued.invite.consumedAt)
        issued.invite.requireUsable()
    }

    @Test
    fun `consume marks the invite as used`() {
        val issued = CustomerInvite.issue(customerId = UUID.randomUUID())
        issued.invite.consume()

        assertNotNull(issued.invite.consumedAt)
        assertFailsWith<AuthenticationException.InviteConsumed> {
            issued.invite.requireUsable()
        }
    }

    @Test
    fun `consume twice fails`() {
        val issued = CustomerInvite.issue(customerId = UUID.randomUUID())
        issued.invite.consume()

        assertFailsWith<AuthenticationException.InviteConsumed> {
            issued.invite.consume()
        }
    }

    @Test
    fun `expired invite cannot be consumed`() {
        val at = Instant.parse("2020-01-01T00:00:00Z")
        val issued = CustomerInvite.issue(
            customerId = UUID.randomUUID(),
            at = at,
            ttl = 1.hours,
        )

        assertFailsWith<AuthenticationException.InviteExpired> {
            issued.invite.consume(at = at + 2.hours)
        }
    }

    @Test
    fun `revoke closes an open invite`() {
        val issued = CustomerInvite.issue(customerId = UUID.randomUUID())
        issued.invite.revoke()

        assertNotNull(issued.invite.consumedAt)
        issued.invite.revoke()
        assertNotNull(issued.invite.consumedAt)
    }

    @Test
    fun `rehydrate restores a consumed invite`() {
        val original = CustomerInvite.issue(customerId = UUID.randomUUID()).invite.apply { consume() }
        val restored = CustomerInvite.rehydrate(
            id = original.id,
            customerId = original.customerId,
            tokenHash = original.tokenHash,
            expiresAt = original.expiresAt,
            consumedAt = original.consumedAt,
            createdAt = original.createdAt,
        )

        assertEquals(
            expected = original.id,
            actual = restored.id,
        )
        assertFailsWith<AuthenticationException.InviteConsumed> {
            restored.requireUsable()
        }
    }
}
