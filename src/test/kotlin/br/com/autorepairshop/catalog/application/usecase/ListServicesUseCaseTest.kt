package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ListServicesUseCaseTest {
    private val services = mockk<ServiceRepository>()
    private val useCase = ListServicesUseCase(services = services)

    @Test
    fun `maps persisted services to responses`() {
        val first = CatalogFixtures.activeService()
        val second = CatalogFixtures.activeService()
        every { services.findAll() } returns listOf(element = first).plus(element = second)

        val response = useCase.execute(input = Unit)

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
    fun `returns empty list when there are no services`() {
        every { services.findAll() } returns emptyList()

        val response = useCase.execute(input = Unit)

        assertTrue(response.isEmpty())
    }
}
