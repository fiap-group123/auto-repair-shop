package br.com.autorepairshop.api.dto.accessidentity

data class LoginRequest(
    val email: String,
    val password: String,
)
