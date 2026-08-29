package br.com.autorepairshop.catalog.domain.valueobject

import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class ServiceName private constructor(val value: String) : ValueObject {
    companion object {
        private val WHITESPACE = Regex(pattern = "\\s+")

        fun of(raw: String): ServiceName {
            val normalized = raw.trim().replace(
                regex = WHITESPACE,
                replacement = " ",
            )
            if (normalized.length !in 2..60) {
                throw CatalogException.InvalidServiceName(
                    message = "Service name must be between 2 and 60 characters.",
                )
            }
            return ServiceName(value = normalized)
        }
    }
}
