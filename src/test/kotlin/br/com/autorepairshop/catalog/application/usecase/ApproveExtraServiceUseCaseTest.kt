package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.event.EventPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ApproveExtraServiceUseCaseTest {
    private val extras = mockk<ExtraServiceRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val events = mockk<EventPublisher>(relaxed = true)
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = ApproveExtraServiceUseCase(
        extras = extras,
        orders = orders,
        events = events,
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
        verify(exactly = 0) { extras.save(extra = any()) }
    }

    @Test
    fun `approves a pending extra and publishes it`() {
        val order = ServiceOrderFixtures.budgetApproved()
        val extra = CatalogFixtures.extraService(serviceOrderId = order.id.value)
        every { extras.findById(id = extra.id) } returns extra
        every { orders.findById(id = order.id) } returns order
        every { extras.save(extra = extra) } returns Unit

        val response = useCase.execute(input = extra.id.value)

        assertEquals(
            expected = "APPROVED",
            actual = response.status,
        )
        verify { extras.save(extra = extra) }
        verify { events.publish(aggregate = extra) }
    }
}
