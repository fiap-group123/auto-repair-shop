package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ListServicesByServiceOrderIdUseCaseTest {
    private val services = mockk<ServiceRepository>()
    private val useCase = ListServicesByServiceOrderIdUseCase(services = services)

    @Test
    fun `maps persisted services of the order to responses`() {
        val serviceOrderId = UUID.randomUUID()
        val first = CatalogFixtures.activeService(serviceOrderId = serviceOrderId)
        val second = CatalogFixtures.activeService(serviceOrderId = serviceOrderId)
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
        val serviceOrderId = UUID.randomUUID()
        every { services.findByServiceOrderId(serviceOrderId = serviceOrderId) } returns emptyList()

        val response = useCase.execute(input = serviceOrderId)

        assertTrue(response.isEmpty())
    }
}
