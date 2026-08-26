package br.com.autorepairshop.catalog

import br.com.autorepairshop.catalog.domain.aggregate.OfferedService
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.shared.domain.Money
import java.math.BigDecimal

object CatalogFixtures {
    const val NAME = "Troca de oleo"
    const val OTHER_NAME = "Alinhamento"
    const val PRICE = "150.00"

    fun money(raw: String = PRICE): Money = Money.of(raw = BigDecimal(raw))

    fun activeService(
        name: String = NAME,
        price: String = PRICE,
    ): OfferedService = OfferedService.register(
        name = ServiceName.of(raw = name),
        price = money(raw = price),
    )

    fun inactiveService(): OfferedService = activeService().apply { deactivate() }
}
