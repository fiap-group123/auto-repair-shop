package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ListServicesByServiceOrderIdUseCaseTest {
    private val services = mockk<ServiceRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = ListServicesByServiceOrderIdUseCase(
        services = services,
        orders = orders,
        access = access,
    )

    @Test
    fun `maps persisted services of the order to responses`() {
        val order = ServiceOrderFixtures.received()
        val serviceOrderId = order.id.value
        val first = CatalogFixtures.activeService(serviceOrderId = serviceOrderId)
        val second = CatalogFixtures.activeService(serviceOrderId = serviceOrderId)
        every { orders.findById(id = ServiceOrderId(value = serviceOrderId)) } returns order
        every { services.findByServiceOrderId(serviceOrderId = serviceOrderId) } returns
            listOf(element = first).plus(element = second)

        val response = useCase.execute(input = serviceOrderId)

        assertEquals(
            expected = 2,
            actual = response.size,
        )
        assertEquals(
            expected = first.id.value,
            actual = response[0].id,
        )
        assertEquals(
            expected = serviceOrderId,
            actual = response[0].serviceOrderId,
        )
        assertEquals(
            expected = second.id.value,
            actual = response[1].id,
        )
    }

    @Test
    fun `returns empty list when the order has no services`() {
        val order = ServiceOrderFixtures.received()
        val serviceOrderId = order.id.value
        every { orders.findById(id = ServiceOrderId(value = serviceOrderId)) } returns order
        every { services.findByServiceOrderId(serviceOrderId = serviceOrderId) } returns emptyList()

        val response = useCase.execute(input = serviceOrderId)

        assertTrue(response.isEmpty())
    }
}
