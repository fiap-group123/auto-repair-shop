package br.com.autorepairshop.accessidentity.infrastructure.persistence

import br.com.autorepairshop.accessidentity.AuthFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class UserRepositoryImplTest {
    private val jpa = mockk<UserJpaRepository>()
    private val repo = UserRepositoryImpl(jpa = jpa)

    @Test
    fun `maps a user through save and queries`() {
        val user = AuthFixtures.client()
        val stored = slot<UserEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(user = user)

        every { jpa.findById(user.id.value) } returns Optional.of(stored.captured)
        every { jpa.findByEmail(email = user.email.value) } returns stored.captured
        every { jpa.existsByEmail(email = user.email.value) } returns true
        every { jpa.existsByCustomerId(customerId = user.customerId!!) } returns true
        every { jpa.findByCustomerId(customerId = user.customerId!!) } returns stored.captured
        every { jpa.count() } returns 1

        assertEquals(
            expected = user.id,
            actual = repo.findById(id = user.id)?.id,
        )
        assertEquals(
            expected = user.id,
            actual = repo.findByEmail(email = user.email)?.id,
        )
        assertTrue(repo.existsByEmail(email = user.email))
        assertTrue(repo.existsByCustomerId(customerId = user.customerId!!))
        assertEquals(
            expected = user.id,
            actual = repo.findByCustomerId(customerId = user.customerId!!)?.id,
        )
        assertTrue(repo.existsAny())
    }

    @Test
    fun `returns null and false when nothing is stored`() {
        val user = AuthFixtures.manager()
        every { jpa.findById(any()) } returns Optional.empty()
        every { jpa.findByEmail(email = any()) } returns null
        every { jpa.existsByEmail(email = any()) } returns false
        every { jpa.existsByCustomerId(customerId = any()) } returns false
        every { jpa.findByCustomerId(customerId = any()) } returns null
        every { jpa.count() } returns 0

        assertNull(repo.findById(id = user.id))
        assertNull(repo.findByEmail(email = user.email))
        assertFalse(repo.existsByEmail(email = user.email))
        assertFalse(repo.existsByCustomerId(customerId = UUID.randomUUID()))
        assertNull(repo.findByCustomerId(customerId = UUID.randomUUID()))
        assertFalse(repo.existsAny())
    }
}
