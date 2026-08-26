package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

@Tag("unit")
class AverageExecutionTimeUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val useCase = AverageExecutionTimeUseCase(orders = orders)

    @Test
    fun `reports an empty sample when nothing was executed`() {
        every { orders.findExecuted() } returns emptyList()

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 0,
            actual = response.sampleSize,
        )
        assertEquals(
            expected = 0.0,
            actual = response.averageSeconds,
            absoluteTolerance = TOLERANCE,
        )
    }

    @Test
    fun `averages the execution time of every measured order`() {
        every { orders.findExecuted() } returns listOf(element = executed(hours = 2))
            .plus(element = executed(hours = 4))

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 2,
            actual = response.sampleSize,
        )
        assertEquals(
            expected = 10_800.0,
            actual = response.averageSeconds,
            absoluteTolerance = TOLERANCE,
        )
    }

    @Test
    fun `ignores orders without a measured execution`() {
        every { orders.findExecuted() } returns listOf(element = ServiceOrderFixtures.inExecution())

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 0,
            actual = response.sampleSize,
        )
    }

    private fun executed(hours: Int): ServiceOrder {
        val order = ServiceOrderFixtures.waitingApproval()
        order.approve(at = Instant.fromEpochSeconds(epochSeconds = 0L))
        order.complete(at = Instant.fromEpochSeconds(epochSeconds = hours.toLong() * 3600))
        return order
    }

    private companion object {
        const val TOLERANCE = 0.001
    }
}
