package br.com.autorepairshop.customer.application.dto.customer

import java.util.UUID

data class CustomerResponse(
    val id: UUID,
    val documentId: String,
    val documentType: String,
    val name: String,
    val email: String,
    val phone: String,
    val active: Boolean,
)
