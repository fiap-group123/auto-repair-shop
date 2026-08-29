package br.com.autorepairshop.api.dto.customer

data class RegisterCustomerRequest(
    val documentId: String,
    val name: String,
    val email: String,
    val phone: String,
)
