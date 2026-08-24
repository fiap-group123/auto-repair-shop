package br.com.autorepairshop.customer.domain.valueobject.vehicle

import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class LicensePlate private constructor(val value: String) : ValueObject {

    val type: LicensePlateType
        get() = if (value[4].isLetter()) LicensePlateType.MERCOSUL else LicensePlateType.NATIONAl

    fun formatted(): String = when (type) {
        LicensePlateType.NATIONAl -> "${value.substring(0, 3)}-${value.substring(3)}"
        LicensePlateType.MERCOSUL -> value
    }

    override fun toString(): String = formatted()

    companion object {
        private val NATIONAL = Regex("^[A-Z]{3}[0-9]{4}$")
        private val MERCOSUL = Regex("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")

        fun of(raw: String): LicensePlate {
            val normalized = normalize(raw)
            if (!isValidNormalized(normalized)) {
                throw VehicleException.InvalidLicensePlate("Invalid license plate: $raw")
            }
            return LicensePlate(normalized)
        }

        fun ofOrNull(raw: String): LicensePlate? =
            normalize(raw).takeIf(::isValidNormalized)?.let(::LicensePlate)

        fun isValid(raw: String): Boolean = isValidNormalized(normalize(raw))

        private fun normalize(raw: String) = raw.uppercase().filter(Char::isLetterOrDigit)

        private fun isValidNormalized(candidate: String) =
            NATIONAL.matches(candidate) || MERCOSUL.matches(candidate)
    }
}
