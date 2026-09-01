package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
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
class FindExtraServiceUseCaseTest {
    private val extras = mockk<ExtraServiceRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = FindExtraServiceUseCase(
        extras = extras,
        orders = orders,
        access = access,
    )

    @Test
    fun `throws when the extra is missing`() {
        val id = UUID.randomUUID()
        every { extras.findById(id = ExtraServiceId(value = id)) } returns null

        assertFailsWith<CatalogException.ExtraServiceNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `throws when the order is missing`() {
        val extra = CatalogFixtures.extraService()
        every { extras.findById(id = extra.id) } returns extra
        every { orders.findById(id = any()) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = extra.id.value)
        }
    }

    @Test
    fun `returns the extra when found`() {
        val order = ServiceOrderFixtures.budgetApproved()
        val extra = CatalogFixtures.extraService(serviceOrderId = order.id.value)
        every { extras.findById(id = extra.id) } returns extra
        every { orders.findById(id = order.id) } returns order

        val response = useCase.execute(input = extra.id.value)

        assertEquals(
            expected = extra.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = extra.serviceOrderId,
            actual = response.serviceOrderId,
        )
        assertEquals(
            expected = "PENDING",
            actual = response.status,
        )
    }
}
