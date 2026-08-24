package br.com.autorepairshop.api.dto

data class RegisterVehicleRequest(
    val plate: String,
    val brand: String,
    val model: String,
    val year: Int,
)
