package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderStatusResponse
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetServiceOrderStatusUseCase(
    private val orders: ServiceOrderRepository,
    private val access: AccessGuard,
) : UseCase<UUID, ServiceOrderStatusResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): ServiceOrderStatusResponse {
        val order = orders.findById(id = ServiceOrderId(value = input))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        access.requireCustomer(customerId = order.customerId)
        return ServiceOrderStatusResponse(
            id = order.id.value,
            status = order.status.name,
            label = order.status.publicLabel(),
        )
    }
}

private fun ServiceOrderStatus.publicLabel(): String = when (this) {
    ServiceOrderStatus.RECEIVED -> "Recebida"
    ServiceOrderStatus.IN_DIAGNOSIS -> "Diagnóstico"
    ServiceOrderStatus.WAITING_APPROVAL -> "Aguardando Aprovação"
    ServiceOrderStatus.BUDGET_APPROVED -> "Orçamento aprovado"
    ServiceOrderStatus.IN_EXECUTION -> "Execução"
    ServiceOrderStatus.FINISHED -> "Finalizada"
    ServiceOrderStatus.DELIVERED -> "Entregue"
    ServiceOrderStatus.BUDGET_REJECTED -> "Orçamento recusado"
}
