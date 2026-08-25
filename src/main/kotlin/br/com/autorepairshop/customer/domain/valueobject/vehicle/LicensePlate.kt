package br.com.autorepairshop.customer.domain.valueobject.vehicle

import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class LicensePlate private constructor(val value: String) : ValueObject {

    val type: LicensePlateType
        get() = if (value[4].isLetter()) LicensePlateType.MERCOSUL else LicensePlateType.NATIONAl

    fun formatted(): String = when (type) {
        LicensePlateType.NATIONAl ->
            "${value.substring(startIndex = 0, endIndex = 3)}-${value.substring(startIndex = 3)}"

        LicensePlateType.MERCOSUL -> value
    }

    override fun toString(): String = formatted()

    companion object {
        private val NATIONAL = Regex(pattern = "^[A-Z]{3}[0-9]{4}$")
        private val MERCOSUL = Regex(pattern = "^[A-Z]{3}[0-9][A-Z][0-9]{2}$")

        fun of(raw: String): LicensePlate {
            val normalized = normalize(raw = raw)
            if (!isValidNormalized(candidate = normalized)) {
                throw VehicleException.InvalidLicensePlate(message = "Invalid license plate: $raw")
            }
            return LicensePlate(value = normalized)
        }

        fun ofOrNull(raw: String): LicensePlate? = normalize(raw = raw)
            .takeIf(predicate = ::isValidNormalized)
            ?.let(block = ::LicensePlate)

        fun isValid(raw: String): Boolean = isValidNormalized(candidate = normalize(raw = raw))

        private fun normalize(raw: String) = raw.uppercase().filter(predicate = Char::isLetterOrDigit)

        private fun isValidNormalized(candidate: String) =
            NATIONAL.matches(input = candidate) || MERCOSUL.matches(input = candidate)
    }
}
