package br.com.autorepairshop.api.dto.customer

import java.util.UUID

data class RegisterVehicleRequest(
    val ownerId: UUID,
    val plate: String,
    val brand: String,
    val model: String,
    val color: String,
    val year: Int,
)
