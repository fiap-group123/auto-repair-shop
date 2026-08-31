package br.com.autorepairshop.budget.application.event

import br.com.autorepairshop.budget.application.usecase.CalculateBudgetTotalUseCase
import br.com.autorepairshop.catalog.domain.event.ServicePriceChanged
import br.com.autorepairshop.catalog.domain.event.ServiceRegistered
import br.com.autorepairshop.catalog.domain.event.ServiceRemoved
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@Tag("unit")
class CalculateBudgetEventListenerTest {
    private val calculateBudgetTotal = mockk<CalculateBudgetTotalUseCase>()
    private val listener = CalculateBudgetEventListener(calculateBudgetTotal = calculateBudgetTotal)

    @Test
    fun `forwards catalog events to the use case`() {
        val serviceOrderId = UUID.randomUUID()
        every { calculateBudgetTotal.execute(input = serviceOrderId) } returns Unit

        listener.on(
            event = ServiceRegistered(
                serviceId = ServiceId.generate(),
                serviceOrderId = serviceOrderId,
                occurredOn = Instant.now(),
            ),
        )
        listener.on(
            event = ServicePriceChanged(
                serviceId = ServiceId.generate(),
                serviceOrderId = serviceOrderId,
                occurredOn = Instant.now(),
            ),
        )
        listener.on(
            event = ServiceRemoved(
                serviceId = ServiceId.generate(),
                serviceOrderId = serviceOrderId,
                occurredOn = Instant.now(),
            ),
        )

        verify(exactly = 3) { calculateBudgetTotal.execute(input = serviceOrderId) }
    }
}
