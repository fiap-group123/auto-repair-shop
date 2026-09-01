package br.com.autorepairshop.serviceandexecution.application.dto

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.inventory.InventoryFixtures
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.toJavaInstant

@Tag("unit")
class ServiceOrderMapperTest {

    @Test
    fun `lists the ids of the order services`() {
        val first = CatalogFixtures.activeService(price = "10.00")
        val second = CatalogFixtures.activeService(price = "20.50")
        val order = ServiceOrderFixtures.inDiagnosis()

        val response = order.toResponse(
            catalog = listOf(element = first).plus(element = second),
        )

        assertEquals(
            expected = listOf(element = first.id.value).plus(element = second.id.value),
            actual = response.serviceIds,
        )
        assertEquals(
            expected = emptyList(),
            actual = response.partIds,
        )
    }

    @Test
    fun `lists the ids of the order parts`() {
        val part = InventoryFixtures.part()
        val order = ServiceOrderFixtures.inDiagnosis()

        val response = order.toResponse(parts = listOf(element = part))

        assertEquals(
            expected = listOf(element = part.id.value),
            actual = response.partIds,
        )
    }

    @Test
    fun `startedAt stays null until diagnosis starts`() {
        val order = ServiceOrderFixtures.received()

        val response = order.toResponse()

        assertNull(response.startedAt)
        assertEquals(
            expected = order.createdAt.toJavaInstant(),
            actual = response.createdAt,
        )
    }
}
