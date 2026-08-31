package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Tag("unit")
class AverageExecutionTimeUseCaseTest {
    private val services = mockk<ServiceRepository>()
    private val useCase = AverageExecutionTimeUseCase(services = services)

    @Test
    fun `returns no average when nothing has finished`() {
        every { services.findAll() } returns listOf(element = CatalogFixtures.activeService())

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 0,
            actual = response.sampleSize,
        )
        assertNull(response.averageSeconds)
    }

    @Test
    fun `averages the finished service durations in seconds`() {
        val started = Instant.fromEpochSeconds(epochSeconds = 1_700_000_000)
        val first = CatalogFixtures.activeService()
        first.inProgress(at = started)
        first.finish(at = started + 2.hours)
        val second = CatalogFixtures.activeService()
        second.inProgress(at = started)
        second.finish(at = started + 4.hours)
        every { services.findAll() } returns listOf(element = first).plus(element = second)

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 2,
            actual = response.sampleSize,
        )
        assertEquals(
            expected = 3.hours.inWholeSeconds,
            actual = response.averageSeconds,
        )
    }
}
