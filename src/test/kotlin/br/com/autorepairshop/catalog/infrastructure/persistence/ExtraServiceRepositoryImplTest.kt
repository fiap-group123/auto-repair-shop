package br.com.autorepairshop.catalog.infrastructure.persistence

import br.com.autorepairshop.catalog.CatalogFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class ExtraServiceRepositoryImplTest {
    private val jpa = mockk<ExtraServiceJpaRepository>()
    private val repo = ExtraServiceRepositoryImpl(jpa = jpa)

    @Test
    fun `maps an extra through save and queries`() {
        val extra = CatalogFixtures.extraService()
        val stored = slot<ExtraServiceEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(extra = extra)

        every { jpa.findById(extra.id.value) } returns Optional.of(stored.captured)
        every {
            jpa.existsByNameAndServiceOrderId(
                name = extra.name.value,
                serviceOrderId = extra.serviceOrderId,
            )
        } returns true
        every { jpa.findAllByServiceOrderId(extra.serviceOrderId) } returns listOf(element = stored.captured)

        assertEquals(
            expected = extra.id,
            actual = repo.findById(id = extra.id)?.id,
        )
        assertTrue(
            repo.existsByName(
                name = extra.name,
                serviceOrderId = extra.serviceOrderId,
            ),
        )
        assertEquals(
            expected = extra.id,
            actual = repo.findByServiceOrderId(serviceOrderId = extra.serviceOrderId).single().id,
        )
        assertEquals(
            expected = extra.status.name,
            actual = repo.findById(id = extra.id)?.status?.name,
        )

        extra.approve()
        extra.inProgress()
        extra.finish()
        repo.save(extra = extra)
        every { jpa.findById(extra.id.value) } returns Optional.of(stored.captured)
        assertEquals(
            expected = extra.status.name,
            actual = repo.findById(id = extra.id)?.status?.name,
        )
        assertEquals(
            expected = extra.startedAt,
            actual = repo.findById(id = extra.id)?.startedAt,
        )
        assertEquals(
            expected = extra.finishedAt,
            actual = repo.findById(id = extra.id)?.finishedAt,
        )
    }

    @Test
    fun `returns empty results when nothing is stored`() {
        every { jpa.findById(any()) } returns Optional.empty()
        every { jpa.findAllByServiceOrderId(any()) } returns emptyList()
        every {
            jpa.existsByNameAndServiceOrderId(
                name = any(),
                serviceOrderId = any(),
            )
        } returns false

        assertNull(repo.findById(id = CatalogFixtures.extraService().id))
        assertTrue(repo.findByServiceOrderId(serviceOrderId = UUID.randomUUID()).isEmpty())
        assertFalse(
            repo.existsByName(
                name = CatalogFixtures.extraService().name,
                serviceOrderId = UUID.randomUUID(),
            ),
        )
    }
}
