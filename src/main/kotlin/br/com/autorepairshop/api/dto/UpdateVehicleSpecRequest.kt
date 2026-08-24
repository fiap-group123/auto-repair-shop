package br.com.autorepairshop.api.dto

data class UpdateVehicleSpecRequest(
    val brand: String,
    val model: String,
    val year: Int,
)
