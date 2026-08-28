package br.com.autorepairshop.catalog.domain.aggregate

import br.com.autorepairshop.catalog.domain.event.ServicePriceChanged
import br.com.autorepairshop.catalog.domain.event.ServiceRegistered
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.catalog.domain.valueobject.ServiceStatus
import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.Money
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class Service private constructor(
    id: ServiceId,
    val serviceOrderId: UUID,
    name: ServiceName,
    basePrice: Money,
    status: ServiceStatus,
    val registeredAt: Instant,
    openedAt: Instant?,
    finishedAt: Instant?,
    estimatedTime: Duration?,
) : AggregateRoot<ServiceId>(id = id) {

    var name: ServiceName = name
        private set

    var basePrice: Money = basePrice
        private set

    var status: ServiceStatus = status
        private set

    var openedAt: Instant? = openedAt
        private set

    var finishedAt: Instant? = finishedAt
        private set

    var estimatedTime: Duration? = estimatedTime
        private set

    fun rename(newName: ServiceName) {
        name = newName
    }

    fun changeBasePrice(
        newBasePrice: Money,
        at: Instant = Clock.System.now(),
    ) {
        basePrice = newBasePrice
        registerEvent(
            event = ServicePriceChanged(
                serviceId = id,
                serviceOrderId = serviceOrderId,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun inProgress(at: Instant = Clock.System.now()) {
        requireStatus(ServiceStatus.WAITING)
        status = ServiceStatus.IN_PROGRESS
        openedAt = at
    }

    fun finish(at: Instant = Clock.System.now()) {
        requireStatus(ServiceStatus.IN_PROGRESS)
        status = ServiceStatus.FINISHED
        finishedAt = at
        openedAt?.let { opened ->
            estimateTime(duration = at - opened)
        }
    }

    private fun estimateTime(duration: Duration) {
        if (duration.isNegative()) {
            throw CatalogException.InvalidDuration(
                message = "Duration cannot be negative.",
            )
        }
        estimatedTime = duration
    }

    private fun requireStatus(expected: ServiceStatus) {
        if (status != expected) {
            throw CatalogException.InvalidStatusTransition(
                message = "Cannot transition from ${status.name}.",
            )
        }
    }

    private fun recordRegistered() {
        registerEvent(
            event = ServiceRegistered(
                serviceId = id,
                serviceOrderId = serviceOrderId,
                occurredOn = registeredAt.toJavaInstant(),
            ),
        )
    }

    companion object {
        fun register(
            serviceOrderId: UUID,
            name: ServiceName,
            price: Money,
            status: ServiceStatus = ServiceStatus.WAITING,
            registeredAt: Instant = Clock.System.now(),
            openedAt: Instant? = null,
            finishedAt: Instant? = null,
            estimatedTime: Duration? = null,
        ): Service {
            val service = Service(
                id = ServiceId.generate(),
                serviceOrderId = serviceOrderId,
                name = name,
                basePrice = price,
                status = status,
                registeredAt = registeredAt,
                openedAt = openedAt,
                finishedAt = finishedAt,
                estimatedTime = estimatedTime,
            )
            service.recordRegistered()
            return service
        }

        internal fun rehydrate(
            id: ServiceId,
            serviceOrderId: UUID,
            name: ServiceName,
            price: Money,
            registeredAt: Instant,
            status: ServiceStatus = ServiceStatus.WAITING,
            openedAt: Instant? = null,
            finishedAt: Instant? = null,
            estimatedTime: Duration? = null,
        ) = Service(
            id = id,
            serviceOrderId = serviceOrderId,
            name = name,
            basePrice = price,
            status = status,
            registeredAt = registeredAt,
            openedAt = openedAt,
            finishedAt = finishedAt,
            estimatedTime = estimatedTime,
        )
    }
}
