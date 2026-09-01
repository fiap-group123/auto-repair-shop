package br.com.autorepairshop.serviceandexecution.application.event

import br.com.autorepairshop.budget.domain.event.BudgetTraded
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.application.dto.toResponse
import br.com.autorepairshop.serviceandexecution.application.usecase.ApproveServiceOrderUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@Tag("unit")
class BudgetTradedEventListenerTest {
    private val approveOrder = mockk<ApproveServiceOrderUseCase>()
    private val listener = BudgetTradedEventListener(approveOrder = approveOrder)

    @Test
    fun `approves the service order when the budget is traded`() {
        val serviceOrderId = UUID.randomUUID()
        every { approveOrder.execute(input = serviceOrderId) } returns
            ServiceOrderFixtures.waitingApproval().toResponse()

        listener.on(
            event = BudgetTraded(
                serviceOrderId = serviceOrderId,
                occurredOn = Instant.now(),
            ),
        )

        verify { approveOrder.execute(input = serviceOrderId) }
    }
}
