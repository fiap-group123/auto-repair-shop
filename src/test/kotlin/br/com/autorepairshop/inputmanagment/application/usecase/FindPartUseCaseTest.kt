package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.PartId
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindPartUseCaseTest {
    private val parts = mockk<PartRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = FindPartUseCase(
        parts = parts,
        orders = orders,
        access = access,
    )

    @Test
    fun `throws when the part is missing`() {
        val id = UUID.randomUUID()
        every { parts.findById(id = PartId(value = id)) } returns null

        assertFailsWith<InventoryException.PartNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `throws when the order is missing`() {
        val part = InventoryFixtures.part()
        every { parts.findById(id = part.id) } returns part
        every { orders.findById(id = any()) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = part.id.value)
        }
    }

    @Test
    fun `returns the part when found`() {
        val order = ServiceOrderFixtures.received()
        val part = InventoryFixtures.part(serviceOrderId = order.id.value)
        every { parts.findById(id = part.id) } returns part
        every { orders.findById(id = order.id) } returns order

        val response = useCase.execute(input = part.id.value)

        assertEquals(
            expected = part.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = order.id.value,
            actual = response.serviceOrderId,
        )
    }
}
