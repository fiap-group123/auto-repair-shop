package br.com.autorepairshop.accessidentity.infrastructure.security

import br.com.autorepairshop.accessidentity.domain.valueobject.HashedPassword
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("unit")
class BcryptPasswordHasherTest {
    private val encoder = mockk<PasswordEncoder>()
    private val hasher = BcryptPasswordHasher(encoder = encoder)

    @Test
    fun `hashes a raw password`() {
        every { encoder.encode("senha123") } returns "hashed"

        assertEquals(
            expected = HashedPassword(value = "hashed"),
            actual = hasher.hash(raw = "senha123"),
        )
    }

    @Test
    fun `rejects a null hash from the encoder`() {
        every { encoder.encode("senha123") } returns null

        assertThrows<IllegalStateException> { hasher.hash(raw = "senha123") }
    }

    @Test
    fun `delegates password matching`() {
        val hashed = HashedPassword(value = "hashed")
        every { encoder.matches("senha123", "hashed") } returns true
        every { encoder.matches("errada", "hashed") } returns false

        assertTrue(actual = hasher.matches(raw = "senha123", hashed = hashed))
        assertFalse(actual = hasher.matches(raw = "errada", hashed = hashed))
    }
}
