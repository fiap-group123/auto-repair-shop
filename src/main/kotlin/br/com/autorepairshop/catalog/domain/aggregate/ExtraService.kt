package br.com.autorepairshop.catalog.domain.aggregate

import br.com.autorepairshop.catalog.domain.event.ExtraServiceApproved
import br.com.autorepairshop.catalog.domain.event.ExtraServiceRegistered
import br.com.autorepairshop.catalog.domain.event.ExtraServiceRejected
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceStatus
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.Money
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class ExtraService private constructor(
    id: ExtraServiceId,
    val serviceOrderId: UUID,
    name: ServiceName,
    basePrice: Money,
    status: ExtraServiceStatus,
    val createdAt: Instant,
) : AggregateRoot<ExtraServiceId>(id = id) {

    var name: ServiceName = name
        private set

    var basePrice: Money = basePrice
        private set

    var status: ExtraServiceStatus = status
        private set

    fun approve(at: Instant = Clock.System.now()) {
        requirePending()
        status = ExtraServiceStatus.APPROVED
        registerEvent(
            event = ExtraServiceApproved(
                extraServiceId = id,
                serviceOrderId = serviceOrderId,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun reject(at: Instant = Clock.System.now()) {
        requirePending()
        status = ExtraServiceStatus.REJECTED
        registerEvent(
            event = ExtraServiceRejected(
                extraServiceId = id,
                serviceOrderId = serviceOrderId,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    private fun requirePending() {
        if (status != ExtraServiceStatus.PENDING) {
            throw CatalogException.InvalidExtraServiceStatusTransition(
                message = "Cannot transition from ${status.name}.",
            )
        }
    }

    private fun recordRegistered() {
        registerEvent(
            event = ExtraServiceRegistered(
                extraServiceId = id,
                serviceOrderId = serviceOrderId,
                occurredOn = createdAt.toJavaInstant(),
            ),
        )
    }

    companion object {
        fun register(
            serviceOrderId: UUID,
            name: ServiceName,
            price: Money,
            createdAt: Instant = Clock.System.now(),
        ): ExtraService {
            val extra = ExtraService(
                id = ExtraServiceId.generate(),
                serviceOrderId = serviceOrderId,
                name = name,
                basePrice = price,
                status = ExtraServiceStatus.PENDING,
                createdAt = createdAt,
            )
            extra.recordRegistered()
            return extra
        }

        internal fun rehydrate(
            id: ExtraServiceId,
            serviceOrderId: UUID,
            name: ServiceName,
            price: Money,
            status: ExtraServiceStatus,
            createdAt: Instant,
        ) = ExtraService(
            id = id,
            serviceOrderId = serviceOrderId,
            name = name,
            basePrice = price,
            status = status,
            createdAt = createdAt,
        )
    }
}
