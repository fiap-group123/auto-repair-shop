package br.com.autorepairshop.accessidentity.domain.aggregate

import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
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
class RefreshSessionTest {

    @Test
    fun `rotate revokes the current session and keeps the family`() {
        val issued = RefreshSession.issue(
            userId = UUID.randomUUID(),
            ttl = 2.hours,
        )
        val rotated = issued.session.rotate(ttl = 2.hours)

        assertNotNull(issued.session.revokedAt)
        assertEquals(
            expected = issued.session.familyId,
            actual = rotated.session.familyId,
        )
        assertEquals(
            expected = rotated.session.id,
            actual = issued.session.replacedBy,
        )
        assertNull(rotated.session.revokedAt)
    }

    @Test
    fun `expired session cannot be rotated`() {
        val at = Instant.parse("2020-01-01T00:00:00Z")
        val issued = RefreshSession.issue(
            userId = UUID.randomUUID(),
            at = at,
            ttl = 1.hours,
        )

        assertFailsWith<AuthenticationException.InvalidRefresh> {
            issued.session.rotate(
                at = at + 2.hours,
                ttl = 1.hours,
            )
        }
    }
}
