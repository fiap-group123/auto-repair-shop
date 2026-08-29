package br.com.autorepairshop.serviceorder.application.dto

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class ServiceOrderMapperTest {

    @Test
    fun `lists the ids of the order services and reports the stored total`() {
        val first = CatalogFixtures.activeService(price = "10.00")
        val second = CatalogFixtures.activeService(price = "20.50")
        val order = ServiceOrderFixtures.inDiagnosisWithBudget()

        val response = order.toResponse(
            catalog = listOf(element = first).plus(element = second),
        )

        assertEquals(
            expected = listOf(element = first.id.value).plus(element = second.id.value),
            actual = response.serviceIds,
        )
        assertEquals(
            expected = ServiceOrderFixtures.TOTAL.amount,
            actual = response.total,
        )
    }

    @Test
    fun `openedAt stays null until diagnosis starts`() {
        val order = ServiceOrderFixtures.received()

        val response = order.toResponse()

        assertNull(response.openedAt)
        assertEquals(
            expected = order.registeredAt,
            actual = response.registeredAt,
        )
        assertEquals(
            expected = "0.00",
            actual = response.total.toPlainString(),
        )
    }
}
