package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.application.dto.toResponse
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListServiceOrdersUseCase(private val orders: ServiceOrderRepository) : UseCase<Unit, List<ServiceOrderResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: Unit): List<ServiceOrderResponse> = orders.findAll().map { it.toResponse() }
}
