package br.com.autorepairshop.accessidentity.application.antilayer

import java.util.UUID

data class CustomerRecord(
    val id: UUID,
    val name: String,
    val email: String,
    val active: Boolean,
)
