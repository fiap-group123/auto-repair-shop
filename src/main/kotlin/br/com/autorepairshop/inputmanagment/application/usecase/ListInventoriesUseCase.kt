package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.application.dto.InventoryResponse
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListInventoriesUseCase(private val inventories: InventoryRepository) :
    UseCase<Unit, List<InventoryResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: Unit): List<InventoryResponse> = inventories.findAll().map { it.toResponse() }
}
