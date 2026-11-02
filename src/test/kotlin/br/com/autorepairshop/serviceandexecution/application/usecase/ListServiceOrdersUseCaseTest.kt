
package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.serviceandexecution.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.serviceandexecution.serviceOrderAssembler
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Instant

@Tag("unit")
class ListServiceOrdersUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val useCase = ListServiceOrdersUseCase(
        orders = orders,
        responses = serviceOrderAssembler(),
    )

    @Test
    fun `keeps the work queue and sorts by status then age`() {
        val finished = opened(status = ServiceOrderStatus.FINISHED, createdAt = "2026-01-01T00:00:00Z")
        val delivered = opened(status = ServiceOrderStatus.DELIVERED, createdAt = "2026-01-01T00:00:00Z")
        val execution = opened(status = ServiceOrderStatus.IN_EXECUTION, createdAt = "2026-06-01T00:00:00Z")
        val approved = opened(status = ServiceOrderStatus.BUDGET_APPROVED, createdAt = "2026-05-01T00:00:00Z")
        val waiting = opened(status = ServiceOrderStatus.WAITING_APPROVAL, createdAt = "2026-04-01T00:00:00Z")
        val diagnosis = opened(status = ServiceOrderStatus.IN_DIAGNOSIS, createdAt = "2026-03-01T00:00:00Z")
        val receivedNew = opened(status = ServiceOrderStatus.RECEIVED, createdAt = "2026-02-02T00:00:00Z")
        val receivedOld = opened(status = ServiceOrderStatus.RECEIVED, createdAt = "2026-02-01T00:00:00Z")
        val rejected = opened(status = ServiceOrderStatus.BUDGET_REJECTED, createdAt = "2026-01-15T00:00:00Z")
        every { orders.findAll() } returns buildList {
            add(element = finished)
            add(element = receivedNew)
            add(element = execution)
            add(element = delivered)
            add(element = approved)
            add(element = waiting)
            add(element = diagnosis)
            add(element = rejected)
            add(element = receivedOld)
        }

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = buildList {
                add(element = execution.id.value)
                add(element = approved.id.value)
                add(element = waiting.id.value)
                add(element = diagnosis.id.value)
                add(element = receivedOld.id.value)
                add(element = receivedNew.id.value)
                add(element = rejected.id.value)
            },
            actual = response.map { order -> order.id },
        )
    }

    private fun opened(
        status: ServiceOrderStatus,
        createdAt: String,
    ): ServiceOrder = ServiceOrder.open(
        customerId = UUID.randomUUID(),
        vehicleId = UUID.randomUUID(),
        status = status,
        createdAt = Instant.parse(createdAt),
    )
}
