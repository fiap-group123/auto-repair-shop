package br.com.autorepairshop.catalog.domain.aggregate

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.event.ServicePriceChanged
import br.com.autorepairshop.catalog.domain.event.ServiceRegistered
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.catalog.domain.valueobject.ServiceStatus
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Tag("unit")
class ServiceTest {

    @Test
    fun `register creates a waiting service with a price`() {
        val serviceOrderId = UUID.randomUUID()
        val service = CatalogFixtures.activeService(serviceOrderId = serviceOrderId)

        assertEquals(
            expected = serviceOrderId,
            actual = service.serviceOrderId,
        )
        assertEquals(
            expected = ServiceStatus.WAITING,
            actual = service.status,
        )
        assertNull(service.estimatedTime)
        assertEquals(
            expected = CatalogFixtures.NAME,
            actual = service.name.value,
        )
        assertEquals(
            expected = "150.00",
            actual = service.basePrice.toString(),
        )
    }

    @Test
    fun `renames and reprices an active service`() {
        val service = CatalogFixtures.activeService()

        service.rename(newName = ServiceName.of(raw = CatalogFixtures.OTHER_NAME))
        service.changeBasePrice(newBasePrice = CatalogFixtures.money(raw = "200.00"))

        assertEquals(
            expected = CatalogFixtures.OTHER_NAME,
            actual = service.name.value,
        )
        assertEquals(
            expected = "200.00",
            actual = service.basePrice.toString(),
        )
    }

    @Test
    fun `inProgress records the start of execution`() {
        val service = CatalogFixtures.activeService()
        val opened = Instant.fromEpochSeconds(epochSeconds = 1_700_000_000)

        service.inProgress(at = opened)

        assertEquals(
            expected = ServiceStatus.IN_PROGRESS,
            actual = service.status,
        )
        assertEquals(
            expected = opened,
            actual = service.openedAt,
        )
    }

    @Test
    fun `finish records the last duration as estimated time`() {
        val service = CatalogFixtures.activeService()
        val opened = Instant.fromEpochSeconds(epochSeconds = 1_700_000_000)
        val finished = opened + 2.hours + 30.minutes

        service.inProgress(at = opened)
        service.finish(at = finished)

        assertEquals(
            expected = ServiceStatus.FINISHED,
            actual = service.status,
        )
        assertEquals(
            expected = finished,
            actual = service.finishedAt,
        )
        assertEquals(
            expected = 2.hours + 30.minutes,
            actual = service.estimatedTime,
        )
    }

    @Test
    fun `finish before openedAt is rejected`() {
        val service = CatalogFixtures.activeService()
        val opened = Instant.fromEpochSeconds(epochSeconds = 1_700_000_000)

        service.inProgress(at = opened)

        assertFailsWith<CatalogException.InvalidDuration> {
            service.finish(at = opened - 1.hours)
        }
    }

    @Test
    fun `rejects status transitions from the wrong status`() {
        val service = CatalogFixtures.activeService()

        assertFailsWith<CatalogException.InvalidStatusTransition> {
            service.finish()
        }

        service.inProgress()
        assertFailsWith<CatalogException.InvalidStatusTransition> {
            service.inProgress()
        }
    }

    @Test
    fun `rehydrate restores the service without domain events`() {
        val original = CatalogFixtures.activeService()
        original.inProgress()
        val restored = Service.rehydrate(
            id = original.id,
            serviceOrderId = original.serviceOrderId,
            name = original.name,
            price = original.basePrice,
            registeredAt = original.registeredAt,
            status = original.status,
            openedAt = original.openedAt,
            finishedAt = original.finishedAt,
            estimatedTime = original.estimatedTime,
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
            expected = ServiceStatus.IN_PROGRESS,
            actual = restored.status,
        )
        assertTrue(restored.domainEvents.isEmpty())
    }

    @Test
    fun `register records the event that lets the order recalculate its budget`() {
        val serviceOrderId = UUID.randomUUID()
        val service = CatalogFixtures.activeService(serviceOrderId = serviceOrderId)

        val event = service.domainEvents.single()

        assertTrue(event is ServiceRegistered)
        assertEquals(
            expected = serviceOrderId,
            actual = event.serviceOrderId,
        )
        assertEquals(
            expected = service.id,
            actual = event.serviceId,
        )
    }

    @Test
    fun `changing the price records an event`() {
        val service = CatalogFixtures.activeService()
        service.clearEvents()

        service.changeBasePrice(newBasePrice = CatalogFixtures.money(raw = "200.00"))

        val event = service.domainEvents.single()

        assertTrue(event is ServicePriceChanged)
        assertEquals(
            expected = service.serviceOrderId,
            actual = event.serviceOrderId,
        )
    }
}
