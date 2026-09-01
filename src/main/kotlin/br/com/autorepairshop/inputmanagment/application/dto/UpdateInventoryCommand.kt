package br.com.autorepairshop.inputmanagment.application.dto

import java.math.BigDecimal
import java.util.UUID

data class UpdateInventoryCommand(
    val inventoryId: UUID,
    val name: String?,
    val unitPrice: BigDecimal?,
    val kind: String?,
)
