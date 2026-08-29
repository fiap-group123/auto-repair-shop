package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RecalculateBudgetTotalUseCase(
    private val orders: ServiceOrderRepository,
    private val services: ServiceRepository,
) : UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        val order = orders.findById(id = ServiceOrderId(value = input))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        val total = services.findByServiceOrderId(serviceOrderId = input)
            .fold(initial = Money.ZERO) { acc, service -> acc.plus(other = service.basePrice) }
        order.updateBudgetTotal(total = total)
        orders.save(order = order)
    }
}
