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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

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
        assertNull(extra.startedAt)
        assertNull(extra.finishedAt)
        assertNull(extra.estimatedTime)
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

        val pending = CatalogFixtures.extraService()
        assertFailsWith<CatalogException.InvalidExtraServiceStatusTransition> {
            pending.inProgress()
        }
        extra.inProgress()
        assertFailsWith<CatalogException.InvalidExtraServiceStatusTransition> {
            extra.inProgress()
        }
        val approved = CatalogFixtures.extraService()
        approved.approve()
        assertFailsWith<CatalogException.InvalidExtraServiceStatusTransition> {
            approved.finish()
        }
    }

    @Test
    fun `inProgress records the start of execution`() {
        val extra = CatalogFixtures.extraService()
        extra.approve()
        val opened = Instant.fromEpochSeconds(epochSeconds = 1_700_000_000)

        extra.inProgress(at = opened)

        assertEquals(
            expected = ExtraServiceStatus.IN_PROGRESS,
            actual = extra.status,
        )
        assertEquals(
            expected = opened,
            actual = extra.startedAt,
        )
    }

    @Test
    fun `finish records the last duration as estimated time`() {
        val extra = CatalogFixtures.extraService()
        extra.approve()
        val opened = Instant.fromEpochSeconds(epochSeconds = 1_700_000_000)
        val finished = opened + 2.hours + 30.minutes

        extra.inProgress(at = opened)
        extra.finish(at = finished)

        assertEquals(
            expected = ExtraServiceStatus.FINISHED,
            actual = extra.status,
        )
        assertEquals(
            expected = finished,
            actual = extra.finishedAt,
        )
        assertEquals(
            expected = 2.hours + 30.minutes,
            actual = extra.estimatedTime,
        )
    }

    @Test
    fun `finish before startedAt is rejected`() {
        val extra = CatalogFixtures.extraService()
        extra.approve()
        val opened = Instant.fromEpochSeconds(epochSeconds = 1_700_000_000)
        extra.inProgress(at = opened)

        assertFailsWith<CatalogException.InvalidDuration> {
            extra.finish(at = opened - 1.hours)
        }
    }

    @Test
    fun `rehydrate restores the extra without domain events`() {
        val original = CatalogFixtures.extraService()
        original.approve()
        original.inProgress()
        val restored = ExtraService.rehydrate(
            id = original.id,
            serviceOrderId = original.serviceOrderId,
            name = original.name,
            price = original.basePrice,
            status = original.status,
            createdAt = original.createdAt,
            startedAt = original.startedAt,
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
            expected = ExtraServiceStatus.IN_PROGRESS,
            actual = restored.status,
        )
        assertTrue(restored.domainEvents.isEmpty())
    }
}
