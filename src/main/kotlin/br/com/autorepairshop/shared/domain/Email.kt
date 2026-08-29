package br.com.autorepairshop.shared.domain

import br.com.autorepairshop.shared.domain.exception.DomainException

@JvmInline
value class Email private constructor(val value: String) : ValueObject {
    companion object {
        private const val MIN_LENGTH = 5
        private const val MAX_LENGTH = 60
        private val PATTERN = Regex(pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

        fun of(raw: String): Email = of(
            raw = raw,
            invalid = { DomainException(message = it) },
        )

        fun <E : DomainException> of(
            raw: String,
            invalid: (String) -> E,
        ): Email {
            val address = raw.trim().lowercase()
            if (address.length !in MIN_LENGTH..MAX_LENGTH) {
                throw invalid("Email address must be between 5 and 60 characters.")
            }
            if (!PATTERN.matches(input = address)) {
                throw invalid("Invalid email address format.")
            }
            return Email(value = address)
        }
    }
}
