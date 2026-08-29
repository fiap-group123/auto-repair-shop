package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.ServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListServicesUseCase(private val services: ServiceRepository) : UseCase<Unit, List<ServiceResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: Unit): List<ServiceResponse> = services.findAll().map { it.toResponse() }
}
