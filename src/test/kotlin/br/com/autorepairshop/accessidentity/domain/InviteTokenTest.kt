package br.com.autorepairshop.accessidentity.domain

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@Tag("unit")
class InviteTokenTest {

    @Test
    fun `hash is deterministic and 64 hex characters`() {
        val hash = InviteToken.hash(raw = "token")

        assertEquals(
            expected = 64,
            actual = hash.length,
        )
        assertEquals(
            expected = hash,
            actual = InviteToken.hash(raw = "token"),
        )
        assertTrue(hash.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun `generate yields distinct raw tokens`() {
        val first = InviteToken.generate()
        val second = InviteToken.generate()

        assertTrue(first.isNotBlank())
        assertNotEquals(
            illegal = first,
            actual = second,
        )
    }
}
