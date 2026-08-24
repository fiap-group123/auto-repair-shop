package br.com.autorepairshop.api.dto

data class UpdateCustomerRequest(
    val name: String,
    val email: String,
    val phone: String,
)
