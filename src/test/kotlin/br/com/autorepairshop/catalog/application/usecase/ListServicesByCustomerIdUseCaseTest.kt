package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ListServicesByCustomerIdUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val services = mockk<ServiceRepository>()
    private val useCase = ListServicesByCustomerIdUseCase(
        orders = orders,
        services = services,
    )

    @Test
    fun `returns empty list when the customer has no orders`() {
        val customerId = UUID.randomUUID()
        every { orders.findByCustomerId(customerId = customerId) } returns emptyList()
        every { services.findByServiceOrderIds(serviceOrderIds = emptyList()) } returns emptyList()

        val response = useCase.execute(input = customerId)

        assertTrue(response.isEmpty())
    }

    @Test
    fun `lists services of all orders of the customer`() {
        val customerId = UUID.randomUUID()
        val firstOrder = ServiceOrderFixtures.received(customerId = customerId)
        val secondOrder = ServiceOrderFixtures.inDiagnosis(customerId = customerId)
        val first = CatalogFixtures.activeService(serviceOrderId = firstOrder.id.value)
        val second = CatalogFixtures.activeService(serviceOrderId = secondOrder.id.value)
        every { orders.findByCustomerId(customerId = customerId) } returns
            listOf(element = firstOrder).plus(element = secondOrder)
        every {
            services.findByServiceOrderIds(
                serviceOrderIds = listOf(element = firstOrder.id.value).plus(element = secondOrder.id.value),
            )
        } returns listOf(element = first).plus(element = second)

        val response = useCase.execute(input = customerId)

        assertEquals(
            expected = 2,
            actual = response.size,
        )
        assertEquals(
            expected = first.id.value,
            actual = response[0].id,
        )
        assertEquals(
            expected = second.id.value,
            actual = response[1].id,
        )
    }
}
