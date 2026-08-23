package br.com.autorepairshop.shared.domain.exception

sealed class DomainException(message: String) : RuntimeException(message)