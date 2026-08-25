package br.com.autorepairshop.api.dto.authentication

data class LoginRequest(
    val email: String,
    val password: String,
)
