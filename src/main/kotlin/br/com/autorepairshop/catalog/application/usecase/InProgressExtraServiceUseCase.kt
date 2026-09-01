package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.ExtraServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class InProgressExtraServiceUseCase(private val extras: ExtraServiceRepository) : UseCase<UUID, ExtraServiceResponse> {

    @Transactional
    override fun execute(input: UUID): ExtraServiceResponse {
        val extra = extras.findById(id = ExtraServiceId(value = input))
            ?: throw CatalogException.ExtraServiceNotFound(message = "Extra service $input was not found.")
        extra.inProgress()
        extras.save(extra = extra)
        return extra.toResponse()
    }
}
