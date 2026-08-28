package br.com.autorepairshop.catalog.domain.aggregate

import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.valueobject.OfferedServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.Money
import kotlin.time.Clock
import kotlin.time.Instant

class OfferedService private constructor(
    id: OfferedServiceId,
    name: ServiceName,
    price: Money,
    active: Boolean,
    val registeredAt: Instant,
) : AggregateRoot<OfferedServiceId>(id = id) {

    var name: ServiceName = name
        private set

    var price: Money = price
        private set

    var active: Boolean = active
        private set

    fun rename(newName: ServiceName) {
        requireActive()
        name = newName
    }

    fun changePrice(newPrice: Money) {
        requireActive()
        price = newPrice
    }

    fun deactivate() {
        requireActive()
        active = false
    }

    fun reactivate() {
        if (active) throw CatalogException.ServiceAlreadyActive(message = "Service is already active.")
        active = true
    }

    private fun requireActive() {
        if (!active) {
            throw CatalogException.ServiceInactive(
                message = "Service ${name.value} is inactive.",
            )
        }
    }

    companion object {
        fun register(
            name: ServiceName,
            price: Money,
            at: Instant = Clock.System.now(),
        ) = OfferedService(
            id = OfferedServiceId.generate(),
            name = name,
            price = price,
            active = true,
            registeredAt = at,
        )

        internal fun rehydrate(
            id: OfferedServiceId,
            name: ServiceName,
            price: Money,
            active: Boolean,
            registeredAt: Instant,
        ) = OfferedService(
            id = id,
            name = name,
            price = price,
            active = active,
            registeredAt = registeredAt,
        )
    }
}
