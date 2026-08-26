package br.com.autorepairshop.serviceorder.domain.valueobject

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Tag("unit")
class ServiceOrderTimelineTest {

    @Test
    fun `a fresh timeline has no execution duration`() {
        assertNull(ServiceOrderTimeline().executionDuration())
    }

    @Test
    fun `execution duration is unknown while the order is not completed`() {
        val timeline = ServiceOrderTimeline(approvedAt = at(hour = 1))

        assertNull(timeline.executionDuration())
    }

    @Test
    fun `execution duration is unknown without an approval`() {
        val timeline = ServiceOrderTimeline(completedAt = at(hour = 4))

        assertNull(timeline.executionDuration())
    }

    @Test
    fun `execution duration spans approval to completion`() {
        val timeline = ServiceOrderTimeline(
            approvedAt = at(hour = 1),
            completedAt = at(hour = 4),
        )

        assertEquals(
            expected = 3.hours,
            actual = timeline.executionDuration(),
        )
    }

    private fun at(hour: Int): Instant = Instant.fromEpochSeconds(epochSeconds = hour.toLong() * 3600)
}
