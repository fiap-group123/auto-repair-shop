package br.com.autorepairshop.accessidentity.application.dto

data class CompleteInviteCommand(
    val token: String,
    val email: String,
    val password: String,
)
