package br.com.autorepairshop.accessidentity.application.dto

data class LoginCommand(
    val email: String,
    val password: String,
)
