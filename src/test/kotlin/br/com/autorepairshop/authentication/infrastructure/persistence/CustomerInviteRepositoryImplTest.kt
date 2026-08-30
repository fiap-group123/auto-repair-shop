package br.com.autorepairshop.authentication.infrastructure.persistence

import br.com.autorepairshop.authentication.domain.aggregate.CustomerInvite
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class CustomerInviteRepositoryImplTest {
    private val jpa = mockk<CustomerInviteJpaRepository>()
    private val repo = CustomerInviteRepositoryImpl(jpa = jpa)

    @Test
    fun `maps an invite through save and queries`() {
        val issued = CustomerInvite.issue(customerId = UUID.randomUUID())
        val stored = slot<CustomerInviteEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(invite = issued.invite)

        every { jpa.findByTokenHash(tokenHash = issued.invite.tokenHash) } returns stored.captured
        every { jpa.findAllByCustomerIdAndConsumedAtIsNull(customerId = issued.invite.customerId) } returns
            listOf(element = stored.captured)

        assertEquals(
            expected = issued.invite.id,
            actual = repo.findByTokenHash(tokenHash = issued.invite.tokenHash)?.id,
        )
        assertEquals(
            expected = 1,
            actual = repo.findOpenByCustomerId(customerId = issued.invite.customerId).size,
        )
    }

    @Test
    fun `returns empty results when nothing is stored`() {
        every { jpa.findByTokenHash(tokenHash = any()) } returns null
        every { jpa.findAllByCustomerIdAndConsumedAtIsNull(customerId = any()) } returns emptyList()

        assertNull(repo.findByTokenHash(tokenHash = "missing"))
        assertTrue(repo.findOpenByCustomerId(customerId = UUID.randomUUID()).isEmpty())
    }
}
