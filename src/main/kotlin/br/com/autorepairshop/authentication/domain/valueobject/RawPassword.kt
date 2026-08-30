package br.com.autorepairshop.authentication.domain.valueobject

import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class RawPassword private constructor(val value: String) : ValueObject {
    companion object {
        private const val MIN_LENGTH = 8

        fun of(raw: String): RawPassword {
            if (raw.length < MIN_LENGTH) {
                throw AuthenticationException.InvalidPassword(
                    message = "Password must be at least $MIN_LENGTH characters.",
                )
            }
            if (raw.none { it.isLetter() } || raw.none { it.isDigit() }) {
                throw AuthenticationException.InvalidPassword(
                    message = "Password must contain at least one letter and one digit.",
                )
            }
            return RawPassword(value = raw)
        }
    }
}
