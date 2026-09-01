package br.com.autorepairshop.inputmanagment.infrastructure.persistence

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class InventoryRepositoryImplTest {
    private val jpa = mockk<InventoryJpaRepository>()
    private val repo = InventoryRepositoryImpl(jpa = jpa)

    @Test
    fun `maps an inventory item through save and queries`() {
        val inventory = InventoryFixtures.inventory()
        val stored = slot<InventoryEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(inventory = inventory)

        every { jpa.findById(inventory.id.value) } returns Optional.of(stored.captured)
        every { jpa.existsByName(name = inventory.name.value) } returns true
        every { jpa.findAll() } returns listOf(element = stored.captured)

        assertEquals(
            expected = inventory.id,
            actual = repo.findById(id = inventory.id)?.id,
        )
        assertTrue(repo.existsByName(name = inventory.name))
        assertEquals(
            expected = inventory.id,
            actual = repo.findAll().single().id,
        )
        assertEquals(
            expected = inventory.type.name,
            actual = repo.findById(id = inventory.id)?.type?.name,
        )
    }

    @Test
    fun `returns empty results when nothing is stored`() {
        every { jpa.findById(any()) } returns Optional.empty()
        every { jpa.findAll() } returns emptyList()
        every { jpa.existsByName(name = any()) } returns false

        assertNull(repo.findById(id = InventoryFixtures.inventory().id))
        assertTrue(repo.findAll().isEmpty())
        assertFalse(repo.existsByName(name = InventoryFixtures.inventory().name))
    }
}
