package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@Tag("unit")
class InProgressExtraServiceUseCaseTest {
    private val extras = mockk<ExtraServiceRepository>()
    private val useCase = InProgressExtraServiceUseCase(extras = extras)

    @Test
    fun `throws when the extra is missing`() {
        val id = UUID.randomUUID()
        every { extras.findById(id = ExtraServiceId(value = id)) } returns null

        assertFailsWith<CatalogException.ExtraServiceNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `throws when the extra is still pending`() {
        val extra = CatalogFixtures.extraService()
        every { extras.findById(id = extra.id) } returns extra

        assertFailsWith<CatalogException.InvalidExtraServiceStatusTransition> {
            useCase.execute(input = extra.id.value)
        }
        verify(exactly = 0) { extras.save(extra = any()) }
    }

    @Test
    fun `starts an approved extra`() {
        val extra = CatalogFixtures.extraService()
        extra.approve()
        every { extras.findById(id = extra.id) } returns extra
        every { extras.save(extra = extra) } returns Unit

        val response = useCase.execute(input = extra.id.value)

        assertEquals(
            expected = ExtraServiceStatus.IN_PROGRESS.name,
            actual = response.status,
        )
        assertNotNull(response.startedAt)
        verify { extras.save(extra = extra) }
    }
}
