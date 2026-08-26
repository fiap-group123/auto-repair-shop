package br.com.autorepairshop.serviceorder.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject
import kotlin.time.Duration
import kotlin.time.Instant

data class ServiceOrderTimeline(
    val diagnosisStartedAt: Instant? = null,
    val diagnosisFinishedAt: Instant? = null,
    val approvedAt: Instant? = null,
    val completedAt: Instant? = null,
    val deliveredAt: Instant? = null,
) : ValueObject {

    /** Time between the budget approval and the end of the execution. */
    fun executionDuration(): Duration? {
        val startedAt = approvedAt ?: return null
        val finishedAt = completedAt ?: return null
        return finishedAt.minus(startedAt)
    }
}
