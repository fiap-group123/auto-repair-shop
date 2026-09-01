package br.com.autorepairshop.accessidentity.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class HashedPassword(val value: String) : ValueObject
