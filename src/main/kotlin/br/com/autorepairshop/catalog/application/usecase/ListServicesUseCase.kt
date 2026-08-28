package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.OfferedServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListOfferedServicesUseCase(private val services: OfferedServiceRepository) :
    UseCase<Unit, List<OfferedServiceResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: Unit): List<OfferedServiceResponse> = services.findAll().map { it.toResponse() }
}
