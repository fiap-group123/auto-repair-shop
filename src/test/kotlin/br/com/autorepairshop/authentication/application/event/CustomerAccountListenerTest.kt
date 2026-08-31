package br.com.autorepairshop.authentication.application.event

import br.com.autorepairshop.authentication.AuthFixtures
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.customer.domain.event.CustomerDeactivated
import br.com.autorepairshop.customer.domain.event.CustomerReactivated
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("unit")
class CustomerAccountListenerTest {
    private val users = mockk<UserRepository>(relaxUnitFun = true)
    private val listener = CustomerAccountListener(users = users)

    @Test
    fun `deactivates the linked user`() {
        val customerId = UUID.randomUUID()
        val user = AuthFixtures.client(customerId = customerId)
        every { users.findByCustomerId(customerId = customerId) } returns user

        listener.on(
            event = CustomerDeactivated(
                customerId = customerId,
                occurredOn = Instant.now(),
            ),
        )

        assertFalse(user.active)
        verify { users.save(user = user) }
    }

    @Test
    fun `reactivates the linked user`() {
        val customerId = UUID.randomUUID()
        val user = AuthFixtures.client(customerId = customerId)
        user.deactivate()
        every { users.findByCustomerId(customerId = customerId) } returns user

        listener.on(
            event = CustomerReactivated(
                customerId = customerId,
                occurredOn = Instant.now(),
            ),
        )

        assertTrue(user.active)
        verify { users.save(user = user) }
    }

    @Test
    fun `skips when the customer has no login`() {
        every { users.findByCustomerId(customerId = any()) } returns null

        listener.on(
            event = CustomerDeactivated(
                customerId = UUID.randomUUID(),
                occurredOn = Instant.now(),
            ),
        )

        verify(exactly = 0) { users.save(user = any()) }
    }
}
