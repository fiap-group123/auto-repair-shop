package br.com.autorepairshop.serviceorder.application.event

import br.com.autorepairshop.budget.domain.event.BudgetRejected
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.application.dto.toResponse
import br.com.autorepairshop.serviceorder.application.usecase.RejectServiceOrderUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@Tag("unit")
class BudgetRejectedEventListenerTest {
    private val rejectOrder = mockk<RejectServiceOrderUseCase>()
    private val listener = BudgetRejectedEventListener(rejectOrder = rejectOrder)

    @Test
    fun `rejects the service order when the budget is rejected`() {
        val serviceOrderId = UUID.randomUUID()
        every { rejectOrder.execute(input = serviceOrderId) } returns
            ServiceOrderFixtures.waitingApproval().toResponse()

        listener.on(
            event = BudgetRejected(
                serviceOrderId = serviceOrderId,
                occurredOn = Instant.now(),
            ),
        )

        verify { rejectOrder.execute(input = serviceOrderId) }
    }
}
