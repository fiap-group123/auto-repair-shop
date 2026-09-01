package br.com.autorepairshop.budget.application.event

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.application.usecase.RegisterBudgetUseCase
import br.com.autorepairshop.serviceandexecution.domain.event.DiagnosisStarted
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@Tag("unit")
class RegisterBudgetEventListenerTest {
    private val registerBudget = mockk<RegisterBudgetUseCase>()
    private val listener = RegisterBudgetEventListener(registerBudget = registerBudget)

    @Test
    fun `registers a budget when diagnosis starts`() {
        val serviceOrderId = UUID.randomUUID()
        every { registerBudget.execute(input = serviceOrderId) } returns
            BudgetFixtures.pricedResponse(serviceOrderId = serviceOrderId)

        listener.on(
            event = DiagnosisStarted(
                serviceOrderId = serviceOrderId,
                occurredOn = Instant.now(),
            ),
        )

        verify { registerBudget.execute(input = serviceOrderId) }
    }
}
