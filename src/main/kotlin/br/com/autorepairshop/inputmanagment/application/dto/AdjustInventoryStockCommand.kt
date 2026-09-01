package br.com.autorepairshop.inputmanagment.application.dto

import java.util.UUID

data class AdjustInventoryStockCommand(
    val inventoryId: UUID,
    val quantity: Int,
)
