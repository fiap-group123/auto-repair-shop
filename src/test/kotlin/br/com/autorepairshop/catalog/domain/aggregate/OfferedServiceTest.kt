package br.com.autorepairshop.catalog.domain.aggregate

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("unit")
class OfferedServiceTest {

    @Test
    fun `register creates an active service with a price`() {
        val service = CatalogFixtures.activeService()

        assertTrue(service.active)
        assertEquals(
            expected = CatalogFixtures.NAME,
            actual = service.name.value,
        )
        assertEquals(
            expected = "150.00",
            actual = service.price.toString(),
        )
    }

    @Test
    fun `renames and reprices an active service`() {
        val service = CatalogFixtures.activeService()

        service.rename(newName = ServiceName.of(raw = CatalogFixtures.OTHER_NAME))
        service.changePrice(newPrice = CatalogFixtures.money(raw = "200.00"))

        assertEquals(
            expected = CatalogFixtures.OTHER_NAME,
            actual = service.name.value,
        )
        assertEquals(
            expected = "200.00",
            actual = service.price.toString(),
        )
    }

    @Test
    fun `deactivate blocks mutations`() {
        val service = CatalogFixtures.activeService()
        service.deactivate()

        assertFalse(service.active)
        assertFailsWith<CatalogException.ServiceInactive> {
            service.rename(newName = ServiceName.of(raw = CatalogFixtures.OTHER_NAME))
        }
        assertFailsWith<CatalogException.ServiceInactive> {
            service.changePrice(newPrice = CatalogFixtures.money(raw = "200.00"))
        }
    }

    @Test
    fun `second deactivate fails`() {
        val service = CatalogFixtures.inactiveService()

        assertFailsWith<CatalogException.ServiceInactive> {
            service.deactivate()
        }
    }

    @Test
    fun `reactivate on an active service fails`() {
        val service = CatalogFixtures.activeService()

        assertFailsWith<CatalogException.ServiceAlreadyActive> {
            service.reactivate()
        }
    }

    @Test
    fun `reactivate restores an inactive service`() {
        val service = CatalogFixtures.inactiveService()

        service.reactivate()

        assertTrue(service.active)
    }

    @Test
    fun `rehydrate restores an inactive service from persistence`() {
        val original = CatalogFixtures.inactiveService()
        val restored = OfferedService.rehydrate(
            id = original.id,
            name = original.name,
            price = original.price,
            active = false,
            registeredAt = original.registeredAt,
        )

        assertFalse(restored.active)
        assertEquals(
            expected = original.id,
            actual = restored.id,
        )
        restored.reactivate()
        assertTrue(restored.active)
    }
}
