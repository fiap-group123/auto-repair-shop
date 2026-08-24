package br.com.autorepairshop.api.dto

data class UpdateVehicleSpecRequest(
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
)
