package br.com.autorepairshop.authentication.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class HashedPassword(val value: String) : ValueObject
