package br.com.autorepairshop.serviceorder.application.event

import br.com.autorepairshop.budget.domain.event.BudgetApproved
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.application.dto.toResponse
import br.com.autorepairshop.serviceorder.application.usecase.ApproveServiceOrderUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@Tag("unit")
class BudgetApprovedEventListenerTest {
    private val approveOrder = mockk<ApproveServiceOrderUseCase>()
    private val listener = BudgetApprovedEventListener(approveOrder = approveOrder)

    @Test
    fun `approves the service order when the budget is approved`() {
        val serviceOrderId = UUID.randomUUID()
        every { approveOrder.execute(input = serviceOrderId) } returns
            ServiceOrderFixtures.waitingApproval().toResponse()

        listener.on(
            event = BudgetApproved(
                serviceOrderId = serviceOrderId,
                occurredOn = Instant.now(),
            ),
        )

        verify { approveOrder.execute(input = serviceOrderId) }
    }
}
