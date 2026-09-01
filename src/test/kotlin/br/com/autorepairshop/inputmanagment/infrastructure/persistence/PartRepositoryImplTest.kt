package br.com.autorepairshop.inputmanagment.infrastructure.persistence

import br.com.autorepairshop.inputmanagment.InventoryFixtures
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
class PartRepositoryImplTest {
    private val jpa = mockk<PartJpaRepository>()
    private val repo = PartRepositoryImpl(jpa = jpa)

    @Test
    fun `maps a part through save and queries`() {
        val part = InventoryFixtures.part()
        val stored = slot<PartEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(part = part)

        every { jpa.findById(part.id.value) } returns Optional.of(stored.captured)
        every {
            jpa.existsByInventoryIdAndServiceOrderId(
                inventoryId = part.inventoryId.value,
                serviceOrderId = part.serviceOrderId,
            )
        } returns true
        every { jpa.findAllByServiceOrderId(part.serviceOrderId) } returns listOf(element = stored.captured)
        every { jpa.findAllByServiceOrderIdIn(listOf(element = part.serviceOrderId)) } returns
            listOf(element = stored.captured)

        assertEquals(
            expected = part.id,
            actual = repo.findById(id = part.id)?.id,
        )
        assertTrue(
            repo.existsByInventoryId(
                inventoryId = part.inventoryId,
                serviceOrderId = part.serviceOrderId,
            ),
        )
        assertEquals(
            expected = part.id,
            actual = repo.findByServiceOrderId(serviceOrderId = part.serviceOrderId).single().id,
        )
        assertEquals(
            expected = part.id,
            actual = repo.findByServiceOrderIds(serviceOrderIds = listOf(element = part.serviceOrderId)).single().id,
        )

        every { jpa.deleteById(part.id.value) } returns Unit
        repo.delete(part = part)
        verify { jpa.deleteById(part.id.value) }
    }

    @Test
    fun `returns empty results when nothing is stored`() {
        every { jpa.findById(any()) } returns Optional.empty()
        every { jpa.findAllByServiceOrderIdIn(any()) } returns emptyList()
        every {
            jpa.existsByInventoryIdAndServiceOrderId(
                inventoryId = any(),
                serviceOrderId = any(),
            )
        } returns false

        assertNull(repo.findById(id = InventoryFixtures.part().id))
        assertTrue(repo.findByServiceOrderIds(serviceOrderIds = emptyList()).isEmpty())
        assertTrue(repo.findByServiceOrderIds(serviceOrderIds = listOf(element = UUID.randomUUID())).isEmpty())
        assertFalse(
            repo.existsByInventoryId(
                inventoryId = InventoryFixtures.inventory().id,
                serviceOrderId = UUID.randomUUID(),
            ),
        )
    }
}
