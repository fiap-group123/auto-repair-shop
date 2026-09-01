package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.catalog.application.dto.ExtraServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RejectExtraServiceUseCase(
    private val extras: ExtraServiceRepository,
    private val orders: ServiceOrderRepository,
    private val events: EventPublisher,
    private val access: AccessGuard,
) : UseCase<UUID, ExtraServiceResponse> {

    @Transactional
    override fun execute(input: UUID): ExtraServiceResponse {
        val extra = extras.findById(id = ExtraServiceId(value = input))
            ?: throw CatalogException.ExtraServiceNotFound(message = "Extra service $input was not found.")
        val order = orders.findById(id = ServiceOrderId(value = extra.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${extra.serviceOrderId} was not found.",
            )
        access.requireCustomer(customerId = order.customerId)
        extra.reject()
        extras.save(extra = extra)
        events.publish(aggregate = extra)
        return extra.toResponse()
    }
}
