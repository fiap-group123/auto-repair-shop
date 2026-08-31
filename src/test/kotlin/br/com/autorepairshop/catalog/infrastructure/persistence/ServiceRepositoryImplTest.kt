package br.com.autorepairshop.catalog.infrastructure.persistence

import br.com.autorepairshop.catalog.CatalogFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class ServiceRepositoryImplTest {
    private val jpa = mockk<ServiceJpaRepository>()
    private val repo = ServiceRepositoryImpl(jpa = jpa)

    @Test
    fun `maps a service through save and queries`() {
        val service = CatalogFixtures.activeService()
        val stored = slot<ServiceEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(service = service)

        every { jpa.findById(service.id.value) } returns Optional.of(stored.captured)
        every {
            jpa.existsByNameAndServiceOrderId(
                name = service.name.value,
                serviceOrderId = service.serviceOrderId,
            )
        } returns true
        every { jpa.findAll() } returns listOf(element = stored.captured)
        every { jpa.findAllByServiceOrderId(service.serviceOrderId) } returns listOf(element = stored.captured)
        every { jpa.findAllByServiceOrderIdIn(listOf(element = service.serviceOrderId)) } returns
            listOf(element = stored.captured)
        every { jpa.existsByServiceOrderId(service.serviceOrderId) } returns true

        assertEquals(
            expected = service.id,
            actual = repo.findById(id = service.id)?.id,
        )
        assertTrue(
            repo.existsByName(
                name = service.name,
                serviceOrderId = service.serviceOrderId,
            ),
        )
        assertEquals(
            expected = service.id,
            actual = repo.findAll().single().id,
        )
        assertEquals(
            expected = service.id,
            actual = repo.findByServiceOrderId(serviceOrderId = service.serviceOrderId).single().id,
        )
        assertEquals(
            expected = service.id,
            actual = repo.findByServiceOrderIds(serviceOrderIds = listOf(element = service.serviceOrderId)).single().id,
        )
        assertTrue(repo.existsByServiceOrderId(serviceOrderId = service.serviceOrderId))

        every { jpa.deleteById(service.id.value) } returns Unit
        repo.delete(service = service)
        verify { jpa.deleteById(service.id.value) }
    }

    @Test
    fun `returns empty results when nothing is stored`() {
        every { jpa.findById(any()) } returns Optional.empty()
        every { jpa.findAllByServiceOrderIdIn(any()) } returns emptyList()

        assertNull(repo.findById(id = CatalogFixtures.activeService().id))
        assertTrue(repo.findByServiceOrderIds(serviceOrderIds = emptyList()).isEmpty())
        assertTrue(repo.findByServiceOrderIds(serviceOrderIds = listOf(element = UUID.randomUUID())).isEmpty())
        assertFalse(
            run {
                every {
                    jpa.existsByNameAndServiceOrderId(
                        name = any(),
                        serviceOrderId = any(),
                    )
                } returns false
                repo.existsByName(
                    name = CatalogFixtures.activeService().name,
                    serviceOrderId = UUID.randomUUID(),
                )
            },
        )
    }
}
