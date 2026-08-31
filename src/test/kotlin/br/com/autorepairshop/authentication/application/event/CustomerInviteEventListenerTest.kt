package br.com.autorepairshop.authentication.application.event

import br.com.autorepairshop.authentication.application.usecase.IssueCustomerInviteUseCase
import br.com.autorepairshop.customer.domain.event.CustomerRegistered
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@Tag("unit")
class CustomerInviteEventListenerTest {
    private val issueInvite = mockk<IssueCustomerInviteUseCase>()
    private val listener = CustomerInviteEventListener(issueInvite = issueInvite)

    @Test
    fun `issues an invite when a customer is registered`() {
        val customerId = UUID.randomUUID()
        every { issueInvite.execute(input = customerId) } returns Unit

        listener.on(
            event = CustomerRegistered(
                customerId = customerId,
                occurredOn = Instant.now(),
            ),
        )

        verify { issueInvite.execute(input = customerId) }
    }
}
