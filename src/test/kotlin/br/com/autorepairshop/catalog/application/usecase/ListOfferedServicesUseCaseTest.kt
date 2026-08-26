package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ListOfferedServicesUseCaseTest {
    private val services = mockk<OfferedServiceRepository>()
    private val useCase = ListOfferedServicesUseCase(services = services)

    @Test
    fun `maps persisted services to responses`() {
        val active = CatalogFixtures.activeService()
        val inactive = CatalogFixtures.inactiveService()
        every { services.findAll() } returns listOf(element = active).plus(element = inactive)

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 2,
            actual = response.size,
        )
        assertEquals(
            expected = active.id.value,
            actual = response[0].id,
        )
        assertTrue(response[0].active)
        assertEquals(
            expected = false,
            actual = response[1].active,
        )
    }

    @Test
    fun `returns empty list when there are no services`() {
        every { services.findAll() } returns emptyList()

        val response = useCase.execute(input = Unit)

        assertTrue(response.isEmpty())
    }
}
