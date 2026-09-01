package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class ListPartsByServiceOrderIdUseCaseTest {
    private val parts = mockk<PartRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = ListPartsByServiceOrderIdUseCase(
        parts = parts,
        orders = orders,
        access = access,
    )

    @Test
    fun `throws when the order is missing`() {
        val serviceOrderId = UUID.randomUUID()
        every { orders.findById(id = ServiceOrderId(value = serviceOrderId)) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = serviceOrderId)
        }
    }

    @Test
    fun `maps persisted parts of the order to responses`() {
        val order = ServiceOrderFixtures.received()
        val serviceOrderId = order.id.value
        val first = InventoryFixtures.part(serviceOrderId = serviceOrderId)
        val second = InventoryFixtures.part(
            serviceOrderId = serviceOrderId,
            inventory = InventoryFixtures.inventory(name = InventoryFixtures.OTHER_NAME),
        )
        every { orders.findById(id = ServiceOrderId(value = serviceOrderId)) } returns order
        every { parts.findByServiceOrderId(serviceOrderId = serviceOrderId) } returns
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
            expected = second.id.value,
            actual = response[1].id,
        )
    }

    @Test
    fun `returns empty list when the order has no parts`() {
        val order = ServiceOrderFixtures.received()
        val serviceOrderId = order.id.value
        every { orders.findById(id = ServiceOrderId(value = serviceOrderId)) } returns order
        every { parts.findByServiceOrderId(serviceOrderId = serviceOrderId) } returns emptyList()

        val response = useCase.execute(input = serviceOrderId)

        assertTrue(response.isEmpty())
    }
}
