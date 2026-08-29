package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.ServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListServicesByServiceOrderIdUseCase(private val services: ServiceRepository) :
    UseCase<UUID, List<ServiceResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): List<ServiceResponse> =
        services.findByServiceOrderId(serviceOrderId = input).map { it.toResponse() }
}
