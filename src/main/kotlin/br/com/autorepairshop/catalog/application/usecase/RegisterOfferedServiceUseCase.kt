package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.OfferedServiceResponse
import br.com.autorepairshop.catalog.application.dto.RegisterOfferedServiceCommand
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.aggregate.OfferedService
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterOfferedServiceUseCase(private val services: OfferedServiceRepository) :
    UseCase<RegisterOfferedServiceCommand, OfferedServiceResponse> {

    @Transactional
    override fun execute(input: RegisterOfferedServiceCommand): OfferedServiceResponse {
        val name = ServiceName.of(raw = input.name)
        if (services.existsByName(name = name)) {
            throw CatalogException.ServiceAlreadyExists(
                message = "Service ${name.value} already exists.",
            )
        }
        val service = OfferedService.register(
            name = name,
            price = Money.of(raw = input.price),
        )
        services.save(service = service)
        return service.toResponse()
    }
}
