package br.com.autorepairshop.api.dto.customer

data class UpdateVehicleSpecRequest(
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
)
