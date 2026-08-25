package br.com.autorepairshop.api.dto.customer

data class UpdateCustomerRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
)
