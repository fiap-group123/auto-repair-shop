package br.com.autorepairshop.catalog.domain.aggregate

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.event.ExtraServiceApproved
import br.com.autorepairshop.catalog.domain.event.ExtraServiceRegistered
import br.com.autorepairshop.catalog.domain.event.ExtraServiceRejected
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceStatus
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class ExtraServiceTest {

    @Test
    fun `register creates a pending extra and records the event`() {
        val serviceOrderId = UUID.randomUUID()
        val extra = CatalogFixtures.extraService(serviceOrderId = serviceOrderId)

        assertEquals(
            expected = serviceOrderId,
            actual = extra.serviceOrderId,
        )
        assertEquals(
            expected = ExtraServiceStatus.PENDING,
            actual = extra.status,
        )
        assertEquals(
            expected = CatalogFixtures.OTHER_NAME,
            actual = extra.name.value,
        )
        assertEquals(
            expected = "150.00",
            actual = extra.basePrice.toString(),
        )
        val event = extra.domainEvents.single()
        assertTrue(event is ExtraServiceRegistered)
        assertEquals(
            expected = serviceOrderId,
            actual = event.serviceOrderId,
        )
        assertEquals(
            expected = extra.id,
            actual = event.extraServiceId,
        )
    }

    @Test
    fun `approve records the event that lets the budget recalculate`() {
        val extra = CatalogFixtures.extraService()
        extra.clearEvents()

        extra.approve()

        assertEquals(
            expected = ExtraServiceStatus.APPROVED,
            actual = extra.status,
        )
        val event = extra.domainEvents.single()
        assertTrue(event is ExtraServiceApproved)
        assertEquals(
            expected = extra.serviceOrderId,
            actual = event.serviceOrderId,
        )
    }

    @Test
    fun `reject records the event that lets the budget recalculate`() {
        val extra = CatalogFixtures.extraService()
        extra.clearEvents()

        extra.reject()

        assertEquals(
            expected = ExtraServiceStatus.REJECTED,
            actual = extra.status,
        )
        val event = extra.domainEvents.single()
        assertTrue(event is ExtraServiceRejected)
        assertEquals(
            expected = extra.serviceOrderId,
            actual = event.serviceOrderId,
        )
    }

    @Test
    fun `rejects status transitions from the wrong status`() {
        val extra = CatalogFixtures.extraService()
        extra.approve()

        assertFailsWith<CatalogException.InvalidExtraServiceStatusTransition> {
            extra.approve()
        }
        assertFailsWith<CatalogException.InvalidExtraServiceStatusTransition> {
            extra.reject()
        }
    }

    @Test
    fun `rehydrate restores the extra without domain events`() {
        val original = CatalogFixtures.extraService()
        original.approve()
        val restored = ExtraService.rehydrate(
            id = original.id,
            serviceOrderId = original.serviceOrderId,
            name = original.name,
            price = original.basePrice,
            status = original.status,
            createdAt = original.createdAt,
        )

        assertEquals(
            expected = original.id,
            actual = restored.id,
        )
        assertEquals(
            expected = original.serviceOrderId,
            actual = restored.serviceOrderId,
        )
        assertEquals(
            expected = ExtraServiceStatus.APPROVED,
            actual = restored.status,
        )
        assertTrue(restored.domainEvents.isEmpty())
    }
}
